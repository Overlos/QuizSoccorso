package com.example.quizsoccorso

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Mostra l'elenco delle domande sbagliate (o senza risposta) al termine di un esame.
 * Permette di confrontare la risposta data dall'utente con quella corretta e leggere la spiegazione.
 */
@Composable
fun ReviewScreen(
    questions: List<QuizQuestion>,
    userAnswers: Map<Int, String>,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    // Filtriamo solo le domande dove la risposta data è diversa da quella corretta
    val wrongQuestions = questions.filter {
        userAnswers[it.id] != it.correct
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Domande sbagliate (${wrongQuestions.size}/${questions.size})",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (wrongQuestions.isEmpty()) {
            Text(
                text = "Nessun errore, ottimo lavoro! 🎉",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            // Mostriamo una card per ogni errore
            wrongQuestions.forEach { question ->
                val given = userAnswers[question.id] ?: "Nessuna risposta data"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = question.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = question.question,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Risposta data dall'utente (in rosso)
                        Text(
                            text = "La tua risposta: $given",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFC62828)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Risposta corretta (in verde)
                        Text(
                            text = "Risposta corretta: ${question.correct}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32)
                        )

                        // Spiegazione se presente
                        if (question.explanation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Perché?",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = question.explanation,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Fonte bibliografica se presente
                        if (question.source.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Fonte: ${question.source}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack
        ) {
            Text("Torna al risultato")
        }
    }
}
