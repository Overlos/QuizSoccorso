package com.example.quizsoccorso

/**
 * Una sezione raggruppa le domande in base ai loro "tags" (es. "SSE", "Autisti").
 *
 * `tags = null` significa "nessun filtro": tutte le domande, indipendentemente
 * dal tag (usato per la sezione "Quiz Misto").
 *
 * Per aggiungere una nuova sezione in futuro basta aggiungere una voce alla
 * lista [defaultQuizSections]: nessun altro punto del codice va modificato,
 * la scelta compare automaticamente nella pagina principale.
 */
data class QuizSection(
    val id: String,
    val label: String,
    val tags: List<String>? = null
)

val defaultQuizSections: List<QuizSection> = listOf(
    QuizSection(id = "sse", label = "Quiz SSE", tags = listOf("SSE")),
    QuizSection(id = "autisti", label = "Quiz Autisti", tags = listOf("Autisti")),
    QuizSection(id = "misto", label = "Quiz Misto", tags = null)
)
