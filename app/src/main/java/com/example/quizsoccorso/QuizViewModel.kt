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
    val details: String,
)

/**
 * ViewModel che gestisce la logica di business per i quiz e gli esami.
 *
 * SCOPO DIDATTICO:
 * Il ViewModel è il "cervello" della UI. Mantiene lo stato (StateFlow) e 
 * reagisce agli input dell'utente invocando il Repository.
 * Implementa gli algoritmi SMART e il calcolo dell'indice di preparazione.
 */
class QuizViewModel(
    private val repository: QuizRepository,
) : ViewModel() {

    companion object {
        // Tempo allocato per singola domanda durante l'esame (40 minuti / 30 domande = 80s)
        private const val SECONDS_PER_EXAM_QUESTION = 80 
    }

    // Database completo in memoria per accesso rapido
    private val _databaseQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val databaseQuestionsFlow: StateFlow<List<QuizQuestion>> = _databaseQuestions.asStateFlow()
    val databaseQuestions: List<QuizQuestion> get() = _databaseQuestions.value

    // Elenco domande della sessione attiva
    private var allQuestions: List<QuizQuestion> = emptyList()
    
    // Gestione del Timer Esame tramite Coroutine Job
    private var timerJob: Job? = null

    // Mappa persistente delle statistiche (ID -> Statistica)
    private val _questionStats = MutableStateFlow<Map<Int, QuestionStat>>(emptyMap())
    val questionStats: StateFlow<Map<Int, QuestionStat>> = _questionStats.asStateFlow()

    // Stato principale della UI (Unica fonte di verità per la vista)
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _isDatabaseAltered = MutableStateFlow(value = false)
    val isDatabaseAltered: StateFlow<Boolean> = _isDatabaseAltered.asStateFlow()

    init {
        // Caricamento asincrono iniziale
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val questions = repository.loadQuestions()
            _databaseQuestions.value = questions
            allQuestions = questions
            _questionStats.value = repository.loadStats()
            checkDatabaseAltered()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun checkDatabaseAltered() {
        val modified = repository.getModifiedQuestions()
        _isDatabaseAltered.value = modified.isNotEmpty()
    }

    fun importDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.importDatabase(uri)
            val questions = repository.loadQuestions()
            _databaseQuestions.value = questions
            allQuestions = questions
            checkDatabaseAltered()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Carica un quiz standard filtrato per tag.
     */
    fun loadQuiz(mode: QuizMode, tags: List<String>? = null, count: Int? = null) {
        val filtered = filterByTags(databaseQuestions, tags).shuffled()
        val questions = if (count != null) filtered.take(count) else filtered
        startQuiz(questions, mode)
    }

    /**
     * Genera un esame garantendo la copertura di ogni capitolo (bilanciamento).
     */
    fun loadExam(number: Int = 30, tags: List<String>? = null) {
        val filtered = filterByTags(databaseQuestions, tags)
        if (filtered.isEmpty()) return

        val chapters = filtered.groupBy { it.category }
        val examQuestions = mutableListOf<QuizQuestion>()
        
        var warning: String? = null
        if (number < chapters.size) {
            warning = "Nota: Hai scelto $number domande ma ci sono ${chapters.size} capitoli. Alcuni capitoli saranno esclusi casualmente."
        }

        // 1. Un quesito per ogni capitolo (se possibile entro il limite 'number')
        val chapterList = chapters.keys.shuffled()
        val chaptersToCover = if (number < chapters.size) chapterList.take(number) else chapterList

        chaptersToCover.forEach { category ->
            examQuestions.add(chapters[category]!!.random())
        }

        // 2. Riempimento fino al numero desiderato (se non abbiamo già raggiunto il limite)
        if (examQuestions.size < number) {
            val remainingCount = number - examQuestions.size
            val pool = (filtered - examQuestions.toSet()).shuffled()
            examQuestions.addAll(pool.take(remainingCount))
        }

        startQuiz(examQuestions.shuffled(), QuizMode.ESAME)
        
        // Impostiamo l'avviso nello stato dopo aver avviato il quiz
        if (warning != null) {
            _uiState.update { it.copy(examWarning = warning) }
        }
    }
    
    fun clearExamWarning() {
        _uiState.update { it.copy(examWarning = null) }
    }

    /**
     * ALGORITMO SMART (Spaced Repetition):
     * Seleziona le domande basandosi su un punteggio di priorità.
     * Studia questa formula: priorità alta = domande difficili + risposte errate + non viste.
     */
    fun loadSmartQuiz(tags: List<String>? = null, count: Int = 20) {
        val stats = _questionStats.value
        val filtered = filterByTags(databaseQuestions, tags)
        
        if (filtered.isEmpty()) return

        val now = System.currentTimeMillis()

        val smartQuestions = filtered
            .asSequence()
            .map { q -> 
                val stat = stats[q.id] ?: QuestionStat()
                val currentDiff = if (stat.userDifficulty > 0) stat.userDifficulty else q.difficulty.toFloat()
                
                // CALCOLO GIORNI DALL'ULTIMA VOLTA (Recenza)
                val daysSinceLastSeen = if (stat.lastSeenTimestamp > 0) {
                    (now - stat.lastSeenTimestamp) / (1000 * 60 * 60 * 24)
                } else {
                    30L // Priorità alta per domande mai viste
                }

                // FORMULA DI PRIORITÀ AGGIORNATA:
                // (1 - Precisione) * 100  -> Pesa gli errori (max 100)
                // (Difficoltà * 10)       -> Pesa la difficoltà intrinseca (max 50)
                // (Recenza * 2)           -> Più tempo è passato, più aumenta la priorità (max 40)
                // (Streak * 15)           -> Sconto se rispondi bene da molte volte
                val priorityScore = ((1f - stat.accuracy) * 100f) + 
                                   (currentDiff * 10f) +
                                   ((daysSinceLastSeen * 2f).coerceAtMost(40f)) - 
                                   (stat.correctStreak * 15f)
                
                q to priorityScore
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(count)
            .toList()
            .shuffled()

        startQuiz(smartQuestions, QuizMode.SMART)
    }

    fun loadQuizByCategory(category: String, mode: QuizMode, tags: List<String>? = null, count: Int? = null) {
        val filtered = filterByTags(databaseQuestions, tags).filter { it.category == category }.shuffled()
        val questions = if (count != null) filtered.take(count) else filtered
        if (questions.isNotEmpty()) startQuiz(questions, mode)
    }

    private fun filterByTags(questions: List<QuizQuestion>, tags: List<String>?): List<QuizQuestion> =
        if (tags.isNullOrEmpty()) questions
        else questions.filter { question -> question.tags.any { it in tags } }

    private fun startQuiz(questions: List<QuizQuestion>, mode: QuizMode) {
        if (questions.isEmpty()) return

        allQuestions = questions
        val first = questions.first()
        val examTime = if (mode == QuizMode.ESAME) questions.size * SECONDS_PER_EXAM_QUESTION else 0

        _uiState.value = QuizUiState(
            currentQuestion = first,
            shuffledAnswers = first.answers.shuffled(),
            totalQuestions = questions.size,
            mode = mode,
            questions = questions,
            alreadyAnsweredBefore = hasAnsweredBefore(first.id),
            totalExamTimeSeconds = examTime,
            remainingTimeSeconds = examTime,
            startTimeMillis = System.currentTimeMillis()
        )

        if (mode == QuizMode.ESAME) startExamTimer(examTime)
    }

    /**
     * Gestisce il tocco dell'utente su una risposta.
     */
    fun answer(index: Int) {
        _uiState.update { state ->
            if (state.answered) return@update state

            val selected = state.shuffledAnswers[index]
            val questionId = state.currentQuestion!!.id

            // Registriamo sempre la risposta data per permettere la revisione finale in ogni modalità
            val updatedUserAnswers = state.userAnswers + (questionId to selected)

            if (state.mode == QuizMode.STUDIO || state.mode == QuizMode.SMART) {
                state.copy(
                    selectedAnswer = selected, 
                    answerSelected = true,
                    userAnswers = updatedUserAnswers
                )
            } else {
                state.copy(
                    selectedAnswer = selected,
                    userAnswers = updatedUserAnswers
                )
            }
        }
    }

    /**
     * Conferma la risposta (Modalità Studio).
     * Salva immediatamente le statistiche perché l'utente potrebbe uscire.
     */
    fun confirmAnswer() {
        _uiState.update { state ->
            if (!state.answerSelected) return@update state

            val question = state.currentQuestion!!
            val correct = question.correct
            val isCorrect = state.selectedAnswer == correct
            
            // Salvataggio immediato in Modalità Studio
            recordAnswer(question, isCorrect, saveToDisk = true)

            state.copy(
                answered = true,
                correctAnswer = correct,
                score = state.score + if (isCorrect) 1 else 0,
                showNextButton = true,
                sessionResults = state.sessionResults + (question.id to isCorrect)
            )
        }
    }

    fun nextQuestion() {
        _uiState.update { state ->
            val nextIndex = state.currentIndex + 1

            if (nextIndex >= allQuestions.size) {
                // Calcolo tempo impiegato per le modalità non-esame (dove non c'è il timer a scalare)
                val duration = if (state.mode != QuizMode.ESAME && state.startTimeMillis > 0) {
                    ((System.currentTimeMillis() - state.startTimeMillis) / 1000).toInt()
                } else {
                    state.timeTakenSeconds
                }
                
                return@update state.copy(
                    quizFinished = true,
                    timeTakenSeconds = duration
                )
            }

            val q = allQuestions[nextIndex]
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
     * Conclude l'esame e salva tutte le statistiche in un unico blocco (Batch).
     * Questo ottimizza drasticamente le prestazioni I/O.
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
                    // Aggiorna lo stato in memoria SENZA scrivere su disco ancora
                    recordAnswer(question, isCorrect, saveToDisk = false)
                    results += (question.id to isCorrect)
                }
            }

            // Scrittura finale su disco di tutte le modifiche accumulate
            saveStatsToDisk()

            val timeTaken = (state.totalExamTimeSeconds - state.remainingTimeSeconds).coerceAtLeast(0)

            state.copy(
                score = score,
                quizFinished = true,
                sessionResults = results,
                timeTakenSeconds = timeTaken
            )
        }
    }

    /**
     * CALCOLO INDICE DI PREPARAZIONE:
     * Un punteggio pesato da 0 a 100 che riflette la tua padronanza.
     * Studia i pesi: 40% Precisione, 30% Recenza, 20% Padronanza, 10% Consistenza.
     */
    fun calculatePreparationIndex(): PreparationIndex {
        val stats = _questionStats.value
        val questions = databaseQuestions
        if (questions.isEmpty() || stats.isEmpty()) {
            return PreparationIndex(0, "Nessun dato", "Esegui dei quiz per vedere il tuo indice.")
        }

        val now = System.currentTimeMillis()
        val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L

        var totalCorrect = 0
        var totalAttempts = 0
        var recentCorrect = 0
        var recentAttempts = 0
        var masteredCount = 0

        stats.values.forEach { stat ->
            totalCorrect += stat.correct
            totalAttempts += stat.attempts
            
            if (now - stat.lastSeenTimestamp < sevenDaysMs) {
                recentCorrect += stat.correct
                recentAttempts += stat.attempts
            }
            
            if (stat.correctStreak >= 3) masteredCount++
        }

        val accuracyScore = if (totalAttempts > 0) (totalCorrect.toFloat() / totalAttempts) * 40 else 0f
        val recentScore = if (recentAttempts > 0) (recentCorrect.toFloat() / recentAttempts) * 30 else 0f
        val masteryScore = (masteredCount.toFloat() / questions.size) * 20
        
        val chapterStats = getCategoryStats()
        val weakChapters = chapterStats.count { it.precisionPercent in 0..64 }
        val consistencyScore = (10f - (weakChapters * 1f)).coerceAtLeast(0f)

        val finalScore = (accuracyScore + recentScore + masteryScore + consistencyScore).toInt().coerceIn(0, 100)

        val level = when {
            finalScore >= 95 -> "Ottimo"
            finalScore >= 90 -> "Buono"
            finalScore >= 85 -> "Sufficiente"
            finalScore >= 70 -> "Debole"
            else -> "Insufficiente"
        }

        val details = "Precisione: ${((accuracyScore / 40) * 100).toInt()}% | Padronanza: $masteredCount/${questions.size} domande"
        return PreparationIndex(finalScore, level, details)
    }

    /**
     * Aggrega le statistiche per categoria.
     * Ottimizzato per raggruppare i tag correttamente senza ridondanze.
     */
    fun getCategoryStats(tags: List<String>? = null): List<CategoryStat> {
        val stats = _questionStats.value
        val filtered = filterByTags(databaseQuestions, tags)
        
        return filtered
            .asSequence()
            .flatMap { q -> 
                val tagsToUse = q.tags.ifEmpty { listOf("Senza Tag") }
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

                val precision = if (weightSum > 0) ((weightedCorrect / weightSum) * 100).toInt() else -1

                CategoryStat(
                    category = category,
                    precisionPercent = precision,
                    answeredCount = answered.size,
                    totalCount = questions.size,
                    tags = listOf(tag)
                )
            }
            .sortedWith(compareBy({ it.tags.firstOrNull() }, { it.category }))
            .toList()
    }

    private fun hasAnsweredBefore(questionId: Int): Boolean =
        (_questionStats.value[questionId]?.attempts ?: 0) > 0

    fun resetStats() {
        _questionStats.value = emptyMap()
        viewModelScope.launch { repository.saveStats(emptyMap()) }
    }

    /**
     * Registra l'esito di una risposta e aggiorna la difficoltà dinamica.
     */
    private fun recordAnswer(question: QuizQuestion, correct: Boolean, saveToDisk: Boolean) {
        val currentStats = _questionStats.value.toMutableMap()
        val current = currentStats[question.id] ?: QuestionStat()
        
        // DIFFICOLTÀ DINAMICA:
        // Se rispondi bene, la domanda "pesa" meno (-0.2). Se sbagli, pesa di più (+0.5).
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
        if (saveToDisk) saveStatsToDisk()
    }

    private fun saveStatsToDisk() {
        viewModelScope.launch { repository.saveStats(_questionStats.value) }
    }

    /**
     * TIMER ALTA PRECISIONE:
     * Invece di usare delay(1000), calcoliamo il momento esatto di fine esame.
     * Questo evita che il timer "rallenti" se il dispositivo è sotto carico.
     */
    private fun startExamTimer(durationSeconds: Int) {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            val endTime = System.currentTimeMillis() + (durationSeconds * 1000L)
            
            while (true) {
                val now = System.currentTimeMillis()
                val remaining = ((endTime - now) / 1000).toInt()
                
                if (remaining <= 0) {
                    _uiState.update { it.copy(remainingTimeSeconds = 0) }
                    submitExam()
                    break
                }
                
                _uiState.update { it.copy(remainingTimeSeconds = remaining) }
                delay(250.milliseconds) // Aggiorniamo più spesso del secondo per una UI fluida
            }
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

    fun saveQuestion(question: QuizQuestion) {
        viewModelScope.launch {
            val currentList = _databaseQuestions.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == question.id }
            if (index != -1) currentList[index] = question
            else {
                val newId = if (currentList.isEmpty()) 1 else currentList.maxOf { it.id } + 1
                currentList.add(question.copy(id = newId))
            }
            repository.saveQuestions(currentList)
            _databaseQuestions.value = currentList
            allQuestions = currentList
            checkDatabaseAltered()
        }
    }

    fun deleteQuestion(questionId: Int) {
        viewModelScope.launch {
            val updatedList = _databaseQuestions.value.filter { it.id != questionId }
            repository.saveQuestions(updatedList)
            _databaseQuestions.value = updatedList
            allQuestions = updatedList
            checkDatabaseAltered()
        }
    }

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
