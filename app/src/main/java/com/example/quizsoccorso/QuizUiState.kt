package com.example.quizsoccorso

/**
 * QuizUiState rappresenta lo stato immutabile della UI per la schermata del Quiz.
 * In Jetpack Compose, questo oggetto viene "osservato": ogni volta che un campo cambia,
 * la UI si aggiorna automaticamente per riflettere il nuovo stato.
 *
 * Studia questi campi per capire come viene gestito il flusso del quiz:
 */
data class QuizUiState(
    // La domanda che l'utente sta visualizzando in questo momento
    val currentQuestion: QuizQuestion? = null,
    
    // Le risposte mescolate casualmente (per non avere sempre la corretta nella stessa posizione)
    val shuffledAnswers: List<String> = emptyList(),
    
    // Posizione attuale (0, 1, 2...) e totale delle domande nella sessione
    val currentIndex: Int = 0,
    val totalQuestions: Int = 0,
    
    // Il punteggio corrente (incrementato solo dopo la conferma in modalità Studio o alla fine in Esame)
    val score: Int = 0,
    
    // Stato del flusso di risposta:
    // answered: l'utente ha confermato e visto se è corretta (solo Studio)
    val answered: Boolean = false,
    // selectedAnswer: la risposta su cui l'utente ha cliccato ma non ha ancora confermato
    val selectedAnswer: String? = null,
    // correctAnswer: memorizza la risposta giusta per evidenziarla in verde dopo l'errore
    val correctAnswer: String? = null,
    
    // Controlli di navigazione della UI
    val showNextButton: Boolean = false,
    val answerSelected: Boolean = false,
    val quizFinished: Boolean = false,
    
    // Modalità attuale: STUDIO (apprendimento), ESAME (simulazione), SMART (ripetizione mirata)
    val mode: QuizMode = QuizMode.STUDIO,
    
    // Dati per la Modalità Esame:
    // userAnswers: mappa ID_DOMANDA -> TESTO_RISPOSTA_DATA
    val userAnswers: Map<Int, String> = emptyMap(),
    // Timer: secondi rimanenti prima della consegna automatica
    val remainingTimeSeconds: Int = 0,
    // Tempo totale calcolato all'inizio (es. 30 domande * 80s = 2400s)
    val totalExamTimeSeconds: Int = 0,
    
    // Dialog di conferma per la consegna dell'esame
    val showSubmitDialog: Boolean = false,
    val unansweredCount: Int = 0,
    
    // Dati per il riepilogo finale (ResultScreen e ReviewScreen)
    val questions: List<QuizQuestion> = emptyList(),
    val sessionResults: Map<Int, Boolean> = emptyMap(),
    val timeTakenSeconds: Int = 0,
    
    // Flag ausiliari
    val alreadyAnsweredBefore: Boolean = false, // Indica se la domanda è "nuova" per l'utente
    val isLoading: Boolean = false // Mostra un caricamento durante l'importazione
)
