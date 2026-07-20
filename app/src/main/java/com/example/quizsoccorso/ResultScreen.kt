package com.example.quizsoccorso

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Soglia di superamento esame predefinita: almeno 25 risposte corrette su 30 (circa 83.3%).
 * Utilizziamo queste costanti per calcolare la soglia proporzionale su qualsiasi numero di domande.
 */
private const val EXAM_PASS_NUMERATOR = 25
private const val EXAM_PASS_DENOMINATOR = 30

/**
 * Rappresenta un riepilogo sintetico per ogni capitolo affrontato nella sessione.
 */
private data class CategorySummary(
    val category: String,
    val correct: Int,
    val answered: Int,
)

/**
 * Schermata dei risultati finali mostrata al termine di un quiz o di un esame.
 * Calcola il superamento dell'esame e mostra un riepilogo qualitativo.
 */
@Composable
fun ResultScreen(
    score: Int,
    total: Int,
    mode: QuizMode,
    questions: List<QuizQuestion> = emptyList(),
    sessionResults: Map<Int, Boolean> = emptyMap(),
    timeTakenSeconds: Int = 0,
    onRestart: () -> Unit,
    onReviewClick: () -> Unit = {}
) {
    // Calcolo della percentuale di risposte corrette (arrotondata all'intero più vicino)
    val percentage = if (total > 0) (score * 100) / total else 0

    // Calcolo della soglia di superamento proporzionale (es. 25/30 = 83.3%)
    val passThreshold = (total * EXAM_PASS_NUMERATOR + EXAM_PASS_DENOMINATOR - 1) / EXAM_PASS_DENOMINATOR
    val examPassed = total > 0 && score >= passThreshold

    // Raggruppamento dei risultati per capitolo per mostrare dove l'utente è andato meglio
    val categorySummaries = questions
        .asSequence()
        .groupBy { it.category }
        .map { (category, categoryQuestions) ->
            CategorySummary(
                category = category,
                correct = categoryQuestions.count { sessionResults[it.id] == true },
                answered = categoryQuestions.size
            )
        }
        .filter { it.answered > 0 }
        .sortedBy { it.category }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quiz terminato",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Punteggio numerico e percentuale
        Text(
            text = "$score / $total",
            style = MaterialTheme.typography.displayMedium
        )

        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Etichetta qualitativa (es. "Ottimo", "Sufficiente" o "Da ripassare")
        Text(
            text = precisionLabel(percentage),
            style = MaterialTheme.typography.titleMedium,
            color = when {
                percentage >= 95 -> Color(0xFF1B5E20)
                percentage >= 90 -> Color(0xFF388E3C)
                percentage >= 85 -> Color(0xFFFBC02D)
                else -> Color(0xFFC62828)
            }
        )

        if (mode == QuizMode.ESAME || mode == QuizMode.SMART) {
            Spacer(modifier = Modifier.height(16.dp))

            if (mode == QuizMode.ESAME) {
                // Risultato dell'esame (Superato/Non Superato)
                Text(
                    text = if (examPassed) "✅ ESAME SUPERATO" else "❌ ESAME NON SUPERATO",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (examPassed) Color(0xFF2E7D32) else Color(0xFFC62828)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Indicazione della soglia minima calcolata
                Text(
                    text = "Soglia minima: $passThreshold/$total risposte corrette",
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tempo totale impiegato (mostrato sia per ESAME che per SMART)
            val minutes = timeTakenSeconds / 60
            val seconds = timeTakenSeconds % 60
            Text(
                text = "Tempo impiegato: %02d:%02d".format(minutes, seconds),
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Riepilogo dettagliato per capitolo
        if (categorySummaries.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Riepilogo per capitolo",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    categorySummaries.forEachIndexed { index, summary ->
                        val summaryPercentage = summary.correct * 100 / summary.answered
                        Text(
                            text = "${summary.category}: ${summary.correct}/${summary.answered} ($summaryPercentage%)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (index != categorySummaries.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Azioni finali
        if ((mode == QuizMode.ESAME || mode == QuizMode.SMART) && score < total) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(0.8f),
                onClick = onReviewClick
            ) {
                Text("Vedi errori")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            modifier = Modifier.fillMaxWidth(0.8f),
            onClick = onRestart
        ) {
            Text("Ricomincia")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
