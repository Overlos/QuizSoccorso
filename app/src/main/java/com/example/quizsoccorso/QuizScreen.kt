package com.example.quizsoccorso

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

/**
 * Schermata principale del Quiz (comune a Modalità Studio, SMART ed Esame).
 * Coordina la visualizzazione della domanda, le risposte e il passaggio tra i quesiti.
 */
@Composable
fun QuizScreen(
    state: QuizUiState,
    hapticEnabled: Boolean,
    onAnswerClick: (Int) -> Unit,
    onConfirmAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onSubmitClick: () -> Unit,
    onSubmitExam: () -> Unit,
    onCloseDialog: () -> Unit,
    onReportQuestionError: (QuizQuestion) -> Unit,
    onRestart: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Gestione della vibrazione al tocco (se abilitata nelle impostazioni)
    val handleAnswerClick: (Int) -> Unit = { index ->
        if (hapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        onAnswerClick(index)
    }

    if (state.quizFinished) {
        // Se la sessione è conclusa, decidiamo se mostrare il risultato o la revisione degli errori
        var showReview by remember { mutableStateOf(false) }
        
        // Gestione del tasto indietro per chiudere la revisione e tornare al risultato
        BackHandler(enabled = showReview) {
            showReview = false
        }

        if (showReview) {
            ReviewScreen(
                questions = state.questions,
                userAnswers = state.userAnswers,
                onBack = { showReview = false }
            )
        } else {
            ResultScreen(
                score = state.score,
                total = state.totalQuestions,
                mode = state.mode,
                questions = state.questions,
                sessionResults = state.sessionResults,
                timeTakenSeconds = state.timeTakenSeconds,
                onRestart = onRestart,
                onReviewClick = { showReview = true }
            )
        }
        return
    }

    val question = state.currentQuestion ?: return

    // Dialog di conferma per evitare consegne accidentali in modalità Esame
    if (state.showSubmitDialog) {
        SubmitConfirmDialog(
            unansweredCount = state.unansweredCount,
            onConfirm = {
                onCloseDialog()
                onSubmitExam()
            },
            onDismiss = onCloseDialog
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
    // Intestazione con barra di progresso e timer (se esame)
    QuizHeader(
        mode = state.mode,
        remainingTimeSeconds = state.remainingTimeSeconds,
        currentIndex = state.currentIndex,
        totalQuestions = state.totalQuestions
    )

    // Metadati della domanda (capitolo, difficoltà, tag)
    QuestionInfo(
        question = question, 
        alreadyAnswered = state.alreadyAnsweredBefore,
        onReportError = { onReportQuestionError(question) }
    )

    // Visualizzazione del testo della domanda
    QuestionCard(question = question)

    // Elenco delle opzioni di risposta cliccabili
    AnswersList(
        answers = state.shuffledAnswers,
        answered = state.answered,
        selectedAnswer = state.selectedAnswer,
        correctAnswer = state.correctAnswer,
        mode = state.mode,
        onAnswerClick = handleAnswerClick
    )

    // Pulsanti dinamici (Conferma, Avanti o Inoltra) in base allo stato
    QuizActionButtons(
        state = state,
        onConfirmAnswer = onConfirmAnswer,
        onSubmitClick = onSubmitClick,
        onNextQuestion = onNextQuestion
    )
    }
}

@Composable
private fun QuizHeader(
    mode: QuizMode,
    remainingTimeSeconds: Int,
    currentIndex: Int,
    totalQuestions: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (mode == QuizMode.ESAME) {
            val minutes = remainingTimeSeconds / 60
            val seconds = remainingTimeSeconds % 60
            Text(
                text = String.format("Tempo rimanente: %02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.titleMedium
            )

            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / totalQuestions.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
        } else if (mode == QuizMode.SMART) {
            Text(
                text = "🧠 Modalità SMART (${currentIndex + 1}/$totalQuestions)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / totalQuestions.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun QuestionInfo(
    question: QuizQuestion, 
    alreadyAnswered: Boolean,
    onReportError: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "Categoria: ${question.category}", style = MaterialTheme.typography.labelMedium)
            Text(text = "Difficoltà: ${question.difficulty}/5", style = MaterialTheme.typography.labelMedium)
            if (question.tags.isNotEmpty()) {
                Text(text = "Tag: ${question.tags.joinToString(", ")}", style = MaterialTheme.typography.labelMedium)
            }
            if (alreadyAnswered) {
                Text(text = "🔁 Hai già risposto in precedenza", style = MaterialTheme.typography.labelMedium)
            }
        }
        
        IconButton(onClick = onReportError) {
            Icon(
                imageVector = Icons.Default.BugReport, 
                contentDescription = "Segnala errore in questa domanda",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun QuestionCard(question: QuizQuestion) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = question.category, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = question.question, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun AnswersList(
    answers: List<String>,
    answered: Boolean,
    selectedAnswer: String?,
    correctAnswer: String?,
    mode: QuizMode,
    onAnswerClick: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        answers.forEachIndexed { index, answer ->
            AnswerCard(
                answer = answer,
                answered = answered,
                selectedAnswer = selectedAnswer,
                correctAnswer = correctAnswer,
                mode = mode,
                onClick = { onAnswerClick(index) }
            )
        }
    }
}

@Composable
private fun QuizActionButtons(
    state: QuizUiState,
    onConfirmAnswer: () -> Unit,
    onSubmitClick: () -> Unit,
    onNextQuestion: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (state.mode == QuizMode.STUDIO && !state.answered) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = state.answerSelected,
                onClick = onConfirmAnswer
            ) {
                Text("Conferma")
            }
        }

        if (state.mode == QuizMode.ESAME || state.mode == QuizMode.SMART) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (state.currentIndex == state.totalQuestions - 1) {
                        onSubmitClick()
                    } else {
                        onNextQuestion()
                    }
                }
            ) {
                Text(if (state.currentIndex == state.totalQuestions - 1) "Inoltra risposte" else "Avanti")
            }
        }

        if (state.showNextButton) {
            ExplanationCard(question = state.currentQuestion)
            Button(modifier = Modifier.fillMaxWidth(), onClick = onNextQuestion) {
                Text("Prossima domanda")
            }
        }
    }
}

@Composable
private fun ExplanationCard(question: QuizQuestion?) {
    if (question != null && question.explanation.isNotBlank()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Spiegazione:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = question.explanation, style = MaterialTheme.typography.bodyMedium)
                if (question.source.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Fonte: ${question.source}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun SubmitConfirmDialog(unansweredCount: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Attenzione") },
        text = { Text("Hai ancora $unansweredCount domande senza risposta.\n\nVuoi consegnare comunque?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Consegna") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } }
    )
}
