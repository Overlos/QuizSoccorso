package com.example.quizsoccorso

/**
 * Rappresenta lo stato della UI durante una sessione di Quiz.
 * Utilizzato per rendere la UI reattiva ai cambiamenti nel ViewModel.
 */
data class QuizUiState(
    // La domanda attualmente visualizzata
    val currentQuestion: QuizQuestion? = null,
    // Elenco delle risposte mescolate per la domanda corrente
    val shuffledAnswers: List<String> = emptyList(),
    // Indice della domanda corrente nella sessione
    val currentIndex: Int = 0,
    // Numero totale di domande nella sessione
    val totalQuestions: Int = 0,
    // Punteggio attuale (risposte corrette)
    val score: Int = 0,
    // Indica se l'utente ha già confermato la risposta per la domanda corrente
    val answered: Boolean = false,
    // Il testo della risposta selezionata dall'utente (ma non ancora confermata)
    val selectedAnswer: String? = null,
    // Il testo della risposta corretta (mostrato dopo la conferma in modalità Studio)
    val correctAnswer: String? = null,
    // Indica se mostrare il pulsante "Prossima domanda"
    val showNextButton: Boolean = false,
    // Indica se l'utente ha selezionato una risposta (per abilitare il tasto conferma)
    val answerSelected: Boolean = false,
    // Indica se la sessione di quiz è terminata
    val quizFinished: Boolean = false,
    // Modalità della sessione (STUDIO o ESAME)
    val mode: QuizMode = QuizMode.STUDIO,
    // Mappa degli ID delle domande e delle risposte date dall'utente (per Esame)
    val userAnswers: Map<Int, String> = emptyMap(),
    // Tempo rimanente in secondi (per Esame)
    val remainingTimeSeconds: Int = 0,
    // Indica se mostrare il dialog di conferma invio esame
    val showSubmitDialog: Boolean = false,
    // Conteggio domande non ancora risposte (per il dialog di conferma)
    val unansweredCount: Int = 0,
    // Elenco completo delle domande della sessione (per review finale)
    val questions: List<QuizQuestion> = emptyList(),
    // Indica se la domanda corrente è già stata affrontata in sessioni precedenti
    val alreadyAnsweredBefore: Boolean = false,
    // Esito (corretto/errato) per ogni domanda della sessione corrente
    val sessionResults: Map<Int, Boolean> = emptyMap(),
    // Tempo totale impiegato per completare la sessione
    val timeTakenSeconds: Int = 0,
    // Tempo totale a disposizione per l'esame (calcolato dinamicamente)
    val totalExamTimeSeconds: Int = 0,
    // Indica se il sistema sta caricando dati (es. import database)
    val isLoading: Boolean = false
)
