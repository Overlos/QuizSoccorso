package com.example.quizsoccorso

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color

@Composable
fun AnswerCard(

    answer: String,

    answered: Boolean,

    selectedAnswer: String?,

    correctAnswer: String?,

    mode: QuizMode,

    onClick: () -> Unit

) {

    val colors = when {

        // selezione in entrambe le modalità
        answer == selectedAnswer && !answered ->

            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF90CAF9)
            )


        // correzione solo in modalità studio
        mode == QuizMode.STUDIO &&
                answered &&
                answer == correctAnswer ->

            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF81C784)
            )


        mode == QuizMode.STUDIO &&
                answered &&
                answer == selectedAnswer ->

            CardDefaults.cardColors(
                containerColor =
                    Color(0xFFE57373)
            )


        else ->

            CardDefaults.cardColors()

    }

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !answered) {
                onClick()
            },

        colors = colors,

        border = if (!answered && answer == selectedAnswer) {

            BorderStroke(
                3.dp,
                androidx.compose.ui.graphics.Color(0xFF1565C0)
            )

        } else {

            null

        }

    ) {

        Text(

            text = answer,

            modifier = Modifier.padding(18.dp),

            style = MaterialTheme.typography.bodyLarge

        )

    }

}