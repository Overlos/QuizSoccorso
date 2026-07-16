package com.example.quizsoccorso

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class PreparationIndex(
    val score: Int,
    val level: String,
    val details: String
)

/**
 * ViewModel che gestisce la logica di business per i quiz e gli esami.
 * Coordina lo stato della UI, il timer dell'esame e le statistiche di apprendimento.
 * È il cuore dell'applicazione e implementa gli algoritmi SMART e di calcolo della preparazione.
 */
class QuizViewModel(
    private val repository: QuizRepository
) : ViewModel() {

    companion object {
        // Tempo allocato per singola domanda durante l'esame (40 minuti / 30 domande = 80s)
        private const val SECONDS_PER_EXAM_QUESTION = 80 
    }

    // Elenco completo delle domande caricate dal database JSON
    private val _databaseQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val databaseQuestionsFlow: StateFlow<List<QuizQuestion>> = _databaseQuestions.asStateFlow()

    // Accesso rapido alle domande (versione non-reattiva)
    val databaseQuestions: List<QuizQuestion> get() = _databaseQuestions.value

    // Domande selezionate per la sessione corrente (Quiz o Esame)
    private var allQuestions: List<QuizQuestion> = emptyList()
    private var timerJob: Job? = null

    // Mappa delle statistiche per ogni domanda (ID domanda -> Statistica)
    private val _questionStats = MutableStateFlow<Map<Int, QuestionStat>>(emptyMap())
    val questionStats: StateFlow<Map<Int, QuestionStat>> = _questionStats.asStateFlow()

    // Stato principale della UI del Quiz (domanda corrente, risposte, punteggio, ecc.)
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        // Caricamento asincrono dei dati all'avvio dell'applicazione
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val questions = repository.loadQuestions()
            _databaseQuestions.value = questions
            allQuestions = questions
            _questionStats.value = repository.loadStats()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Importa un database JSON esterno e aggiorna lo stato dell'app.
     */
    fun importDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.importDatabase(uri)
            val questions = repository.loadQuestions()
            _databaseQuestions.value = questions
            allQuestions = questions
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Avvia una sessione di studio filtrata per tag (es. SSE, Autisti).
     */
    fun loadQuiz(mode: QuizMode, tags: List<String>? = null, count: Int? = null) {
        val filtered = filterByTags(databaseQuestions, tags).shuffled()
        val questions = if (count != null) filtered.take(count) else filtered
        startQuiz(questions, mode)
    }

    /**
     * Genera una sessione d'esame bilanciata garantendo almeno una domanda per ogni capitolo.
     */
    fun loadExam(number: Int = 30, tags: List<String>? = null) {
        val filtered = filterByTags(databaseQuestions, tags)
        if (filtered.isEmpty()) return

        val chapters = filtered.groupBy { it.category }
        val examQuestions = mutableListOf<QuizQuestion>()

        // 1. Garantisce la copertura di tutti i capitoli disponibili
        chapters.forEach { (_, questionsInChapter) ->
            examQuestions.add(questionsInChapter.random())
        }

        // 2. Gestione del numero di domande richiesto rispetto ai capitoli presenti
        val finalSelection = if (examQuestions.size > number) {
            examQuestions.shuffled().take(number)
        } else {
            // 3. Riempiamo i posti restanti con domande casuali dal pool filtrato
            val remainingCount = number - examQuestions.size
            val pool = (filtered - examQuestions.toSet()).shuffled()
            examQuestions.addAll(pool.take(remainingCount))
            examQuestions.shuffled()
        }

        startQuiz(finalSelection, QuizMode.ESAME)
    }

    /**
     * Algoritmo SMART: seleziona le domande prioritizzando quelle con bassa precisione,
     * alta difficoltà percepita e non viste di recente (Spaced Repetition).
     */
    fun loadSmartQuiz(tags: List<String>? = null, count: Int = 20) {
        val stats = _questionStats.value
        val filtered = filterByTags(databaseQuestions, tags)
        
        if (filtered.isEmpty()) return

        // Calcolo del punteggio di priorità per ogni domanda
        val smartQuestions = filtered
            .map { q -> 
                val stat = stats[q.id] ?: QuestionStat()
                val currentDiff = if (stat.userDifficulty > 0) stat.userDifficulty else q.difficulty.toFloat()
                
                // FORMULA SMART:
                // + Peso elevato per bassa precisione (Accuracy 0 = 100 punti)
                // + Peso per difficoltà (Difficoltà 5 = 50 punti)
                // - Sconto per quesiti a cui si è risposto correttamente in modo costante (CorrectStreak 3 = -45 punti)
                val priorityScore = ((1f - stat.accuracy) * 100f) + 
                                   (currentDiff * 10f) - 
                                   (stat.correctStreak * 15f)
                
                q to priorityScore
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(count)
            .shuffled()

        startQuiz(smartQuestions, QuizMode.SMART)
    }

    /**
     * Carica le domande appartenenti a una specifica categoria.
     */
    fun loadQuizByCategory(category: String, mode: QuizMode, tags: List<String>? = null, count: Int? = null) {
        val filtered = filterByTags(databaseQuestions, tags).filter { it.category == category }.shuffled()
        val questions = if (count != null) filtered.take(count) else filtered
        if (questions.isNotEmpty()) startQuiz(questions, mode)
    }

    private fun filterByTags(
        questions: List<QuizQuestion>,
        tags: List<String>?
    ): List<QuizQuestion> =
        if (tags.isNullOrEmpty()) questions
        else questions.filter { question -> question.tags.any { it in tags } }

    private fun startQuiz(questions: List<QuizQuestion>, mode: QuizMode) {
        if (questions.isEmpty()) return

        allQuestions = questions
        val first = questions.first()

        // Calcolo tempo proporzionale per l'esame
        val examTime = if (mode == QuizMode.ESAME) questions.size * SECONDS_PER_EXAM_QUESTION else 0

        _uiState.value = QuizUiState(
            currentQuestion = first,
            shuffledAnswers = first.answers.shuffled(),
            totalQuestions = questions.size,
            mode = mode,
            questions = questions,
            alreadyAnsweredBefore = hasAnsweredBefore(first.id),
            totalExamTimeSeconds = examTime,
            remainingTimeSeconds = examTime
        )

        if (mode == QuizMode.ESAME) startExamTimer(examTime)
    }

    /**
     * Gestisce la selezione di una risposta da parte dell'utente.
     */
    fun answer(index: Int) {
        _uiState.update { state ->
            if (state.answered) return@update state

            val selected = state.shuffledAnswers[index]

            if (state.mode == QuizMode.STUDIO) {
                state.copy(selectedAnswer = selected, answerSelected = true)
            } else {
                val questionId = state.currentQuestion!!.id
                state.copy(
                    selectedAnswer = selected,
                    userAnswers = state.userAnswers + (questionId to selected)
                )
            }
        }
    }

    /**
     * Conferma la risposta selezionata (Modalità Studio).
     */
    fun confirmAnswer() {
        _uiState.update { state ->
            if (!state.answerSelected) return@update state

            val question = state.currentQuestion!!
            val correct = question.correct
            val isCorrect = state.selectedAnswer == correct
            val score = state.score + if (isCorrect) 1 else 0

            recordAnswer(question, isCorrect)

            state.copy(
                answered = true,
                correctAnswer = correct,
                score = score,
                showNextButton = true,
                sessionResults = state.sessionResults + (question.id to isCorrect)
            )
        }
    }

    /**
     * Passa alla domanda successiva nella sessione corrente.
     * Aggiorna lo stato della UI con la nuova domanda o termina il quiz.
     */
    fun nextQuestion() {
        _uiState.update { state ->
            val nextIndex = state.currentIndex + 1

            if (nextIndex >= allQuestions.size) {
                // Se abbiamo finito le domande, passiamo allo stato finale
                return@update state.copy(quizFinished = true)
            }

            val q = allQuestions[nextIndex]

            // Creiamo un nuovo stato per la nuova domanda
            state.copy(
                currentQuestion = q,
                shuffledAnswers = q.answers.shuffled(),
                currentIndex = nextIndex,
                answered = false,
                selectedAnswer = null,
                correctAnswer = null,
                showNextButton = false,
                answerSelected = false,
                alreadyAnsweredBefore = hasAnsweredBefore(q.id)
            )
        }
    }

    /**
     * Conclude l'esame, calcola il punteggio finale e salva i risultati.
     */
    fun submitExam() {
        timerJob?.cancel()

        _uiState.update { state ->
            var score = 0
            var results = state.sessionResults

            allQuestions.forEach { question ->
                val given = state.userAnswers[question.id]
                if (given != null) {
                    val isCorrect = given == question.correct
                    if (isCorrect) score++
                    recordAnswer(question, isCorrect)
                    results = results + (question.id to isCorrect)
                }
            }

            val timeTaken =
                (state.totalExamTimeSeconds - state.remainingTimeSeconds).coerceAtLeast(0)

            state.copy(
                score = score,
                quizFinished = true,
                sessionResults = results,
                timeTakenSeconds = timeTaken
            )
        }
    }

    /**
     * Calcola un indice di preparazione globale (0-100) basato su precisione storica,
     * risultati recenti, padronanza delle domande e copertura dei capitoli.
     */
    fun calculatePreparationIndex(): PreparationIndex {
        val stats = _questionStats.value
        val questions = databaseQuestions
        if (questions.isEmpty() || stats.isEmpty()) {
            return PreparationIndex(0, "Nessun dato", "Inizia a rispondere ai quiz per vedere il tuo indice.")
        }

        val now = System.currentTimeMillis()
        val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L

        var totalCorrect = 0
        var totalAttempts = 0
        var recentCorrect = 0
        var recentAttempts = 0
        var masteredCount = 0

        // Analisi delle statistiche persistite
        stats.values.forEach { stat ->
            totalCorrect += stat.correct
            totalAttempts += stat.attempts
            
            // Dati dell'ultima settimana
            if (now - stat.lastSeenTimestamp < sevenDaysMs) {
                recentCorrect += stat.correct
                recentAttempts += stat.attempts
            }
            
            // Una domanda è considerata "padroneggiata" se è stata fornita la risposta corretta per 3 volte consecutive
            if (stat.correctStreak >= 3) {
                masteredCount++
            }
        }

        // CALCOLO PESATO DELL'INDICE (0-100):
        
        // 1. Componente Precisione (40%): Risultato storico complessivo
        val accuracyScore = if (totalAttempts > 0) (totalCorrect.toFloat() / totalAttempts) * 40 else 0f

        // 2. Componente Recenza (30%): Performance dell'ultima settimana (riflette lo studio attuale)
        val recentScore = if (recentAttempts > 0) (recentCorrect.toFloat() / recentAttempts) * 30 else 0f

        // 3. Componente Padronanza (20%): Quante domande sono state consolidate
        val masteryScore = (masteredCount.toFloat() / questions.size) * 20

        // 4. Componente Consistenza (10%): Penalità se ci sono capitoli con bassa precisione (< 65%)
        val chapterStats = getCategoryStats()
        val weakChapters = chapterStats.count { it.precisionPercent in 0..64 }
        val consistencyScore = (10f - (weakChapters * 1f)).coerceAtLeast(0f)

        val finalScore = (accuracyScore + recentScore + masteryScore + consistencyScore).toInt().coerceIn(0, 100)

        // Assegnazione del livello testuale basato sulla nuova soglia dell'85%
        val level = when {
            finalScore >= 95 -> "Ottimo"
            finalScore >= 90 -> "Buono"
            finalScore >= 85 -> "Sufficiente"
            finalScore >= 70 -> "Debole"
            else -> "Insufficiente"
        }

        val details = "Precisione: ${(accuracyScore/40*100).toInt()}% | Padronanza: $masteredCount/${questions.size} domande"

        return PreparationIndex(finalScore, level, details)
    }

    /**
     * Calcola le statistiche aggregate per ogni combinazione di categoria e tag.
     * Questo evita che categorie con lo stesso nome ma tag diversi vengano raggruppate.
     */
    fun getCategoryStats(tags: List<String>? = null): List<CategoryStat> {
        val stats = _questionStats.value
        val filtered = filterByTags(databaseQuestions, tags)
        
        // Esplodiamo le domande per tag per calcolare le statistiche per ogni associazione Tag-Categoria
        return filtered
            .flatMap { q -> 
                val tagsToUse = if (q.tags.isEmpty()) listOf("Senza Tag") else q.tags
                tagsToUse.map { tag -> q to tag }
            }
            .groupBy { (q, tag) -> q.category to tag }
            .map { (key, entries) ->
                val (category, tag) = key
                val questions = entries.map { it.first }
                val answered = questions.filter { (stats[it.id]?.attempts ?: 0) > 0 }

                var weightSum = 0.0
                var weightedCorrect = 0.0

                answered.forEach { question ->
                    val stat = stats[question.id]!!
                    val weight = question.difficulty.toDouble()
                    weightSum += weight
                    weightedCorrect += weight * stat.accuracy
                }

                val precision =
                    if (weightSum > 0) ((weightedCorrect / weightSum) * 100).toInt() else -1

                CategoryStat(
                    category = category,
                    precisionPercent = precision,
                    answeredCount = answered.size,
                    totalCount = questions.size,
                    tags = listOf(tag)
                )
            }
            .sortedWith(compareBy({ it.tags.firstOrNull() }, { it.category }))
    }

    private fun hasAnsweredBefore(questionId: Int): Boolean =
        (_questionStats.value[questionId]?.attempts ?: 0) > 0

    fun resetStats() {
        _questionStats.value = emptyMap()
        viewModelScope.launch {
            repository.saveStats(emptyMap())
        }
    }

    private fun recordAnswer(question: QuizQuestion, correct: Boolean) {
        val currentStats = _questionStats.value.toMutableMap()
        val current = currentStats[question.id] ?: QuestionStat()
        
        // Calcolo Difficoltà Dinamica:
        // Se corretta, la domanda diventa più facile (-0.2), se errata più difficile (+0.5)
        val baseDiff = if (current.userDifficulty > 0) current.userDifficulty else question.difficulty.toFloat()
        val newDifficulty = if (correct) {
            (baseDiff - 0.2f).coerceAtLeast(1.0f)
        } else {
            (baseDiff + 0.5f).coerceAtMost(5.0f)
        }

        currentStats[question.id] = current.copy(
            attempts = current.attempts + 1,
            correct = current.correct + if (correct) 1 else 0,
            lastSeenTimestamp = System.currentTimeMillis(),
            correctStreak = if (correct) current.correctStreak + 1 else 0,
            userDifficulty = newDifficulty
        )
        
        _questionStats.value = currentStats
        viewModelScope.launch {
            repository.saveStats(currentStats)
        }
    }

    /**
     * Avvia il timer per la modalità esame.
     */
    private fun startExamTimer(durationSeconds: Int) {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            var time = durationSeconds

            while (time > 0) {
                delay(1000.milliseconds)
                time--
                _uiState.update { it.copy(remainingTimeSeconds = time) }
            }

            submitExam()
        }
    }

    fun checkBeforeSubmit() {
        val state = _uiState.value
        val missing = allQuestions.count { !state.userAnswers.containsKey(it.id) }

        if (missing > 0) {
            _uiState.update { it.copy(showSubmitDialog = true, unansweredCount = missing) }
        } else {
            submitExam()
        }
    }

    fun closeSubmitDialog() {
        _uiState.update { it.copy(showSubmitDialog = false) }
    }

    /**
     * Aggiunge o aggiorna una domanda nel database.
     */
    fun saveQuestion(question: QuizQuestion) {
        viewModelScope.launch {
            val currentList = _databaseQuestions.value
            val updatedList = currentList.toMutableList()
            val index = updatedList.indexOfFirst { it.id == question.id }
            
            if (index != -1) {
                updatedList[index] = question
            } else {
                val newId = if (currentList.isEmpty()) 1 else currentList.maxOf { it.id } + 1
                updatedList.add(question.copy(id = newId))
            }
            
            repository.saveQuestions(updatedList)
            _databaseQuestions.value = updatedList
            allQuestions = updatedList
        }
    }

    /**
     * Elimina una domanda dal database.
     */
    fun deleteQuestion(questionId: Int) {
        viewModelScope.launch {
            val updatedList = _databaseQuestions.value.filter { it.id != questionId }
            repository.saveQuestions(updatedList)
            _databaseQuestions.value = updatedList
            allQuestions = updatedList
        }
    }

    /**
     * Ritorna tutte le domande nel database per l'editor.
     */
    fun getAllDatabaseQuestions(): List<QuizQuestion> = databaseQuestions

    /**
     * Resetta lo stato del quiz per tornare alla home.
     * Cancella il timer se attivo.
     */
    /**
     * Ritorna le statistiche per ogni singola domanda di una categoria filtrata per tag.
     */
    fun getQuestionStatsByCategory(category: String, tag: String): List<Pair<QuizQuestion, QuestionStat>> {
        val stats = _questionStats.value
        return databaseQuestions.filter { 
            it.category == category && (it.tags.contains(tag) || (tag == "Senza Tag" && it.tags.isEmpty())) 
        }
            .map { it to (stats[it.id] ?: QuestionStat()) }
            .sortedByDescending { it.second.attempts }
    }

    fun resetQuiz() {
        timerJob?.cancel()
        _uiState.value = QuizUiState()
    }
}
