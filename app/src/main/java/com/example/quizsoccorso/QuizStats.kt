package com.example.quizsoccorso

import com.google.gson.annotations.SerializedName

/**
 * Statistica accumulata per una singola domanda, salvata in modo persistente.
 * Viene utilizzata dagli algoritmi SMART per determinare la frequenza di comparsa.
 */
data class QuestionStat(
    @SerializedName("attempts") val attempts: Int = 0,         // Numero di volte che l'utente ha fornito una risposta a questo quesito
    @SerializedName("correct") val correct: Int = 0,          // Numero di risposte corrette registrate
    @SerializedName("lastSeenTimestamp") val lastSeenTimestamp: Long = 0, // Istante temporale dell'ultima visualizzazione
    @SerializedName("correctStreak") val correctStreak: Int = 0,    // Successi consecutivi (Spaced Repetition)
    @SerializedName("userDifficulty") val userDifficulty: Float = -1f // Valutazione della difficoltà basata sulle performance individuali
) {
    /**
     * Calcola la percentuale di precisione (0.0 a 1.0) per questa specifica domanda.
     */
    val accuracy: Float
        get() = if (attempts == 0) 0f else correct.toFloat() / attempts
}

/**
 * Statistica aggregata per un intero capitolo (categoria).
 * Viene calcolata dinamicamente unendo le [QuestionStat] di tutte le domande del capitolo.
 */
data class CategoryStat(
    val category: String,
    val precisionPercent: Int, // Percentuale media di precisione (-1 se mai affrontata)
    val answeredCount: Int,    // Quante domande del capitolo sono state provate almeno una volta
    val totalCount: Int,       // Numero totale di domande presenti nel capitolo
    val tags: List<String> = emptyList() // Tag associati (es. SSE, Autisti)
)

/**
 * Etichetta testuale che indica il livello di preparazione per un capitolo o una domanda.
 * I criteri sono stringenti: la sufficienza richiede almeno l'85% di precisione.
 */
fun precisionLabel(percent: Int): String = when {
    percent < 0 -> "Non ancora affrontato"
    percent >= 95 -> "🟢 Ottimo"
    percent >= 90 -> "🟡 Buono"
    percent >= 85 -> "🟠 Sufficiente"
    else -> "🔴 Da ripassare"
}
