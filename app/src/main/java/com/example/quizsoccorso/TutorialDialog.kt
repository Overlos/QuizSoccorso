package com.example.quizsoccorso

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class TutorialStep(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun TutorialDialog(onDismiss: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(0) }
    
    val steps = listOf(
        TutorialStep(
            "Benvenuto!",
            "Questa applicazione ti guida nella preparazione per i concorsi SSE e Autisti. Iniziamo esplorando le funzionalità principali.",
            Icons.Default.Celebration
        ),
        TutorialStep(
            "Selezione della Sezione",
            "Scegli qui sotto la tua area di interesse (SSE o Autisti). Noterai le icone Medicina e Auto: l'app si adatterà alla tua scelta.",
            Icons.Default.MedicalServices
        ),
        TutorialStep(
            "Modalità Studio",
            "Ideale per consolidare le conoscenze: riceverai feedback immediato e spiegazioni dettagliate per ogni risposta.",
            Icons.Default.School
        ),
        TutorialStep(
            "Quiz SMART",
            "Il sistema analizza le tue performance e propone i quesiti che richiedono maggior approfondimento.",
            Icons.Default.Psychology
        ),
        TutorialStep(
            "Simulazione Esame",
            "Mettiti alla prova con un timer reale (80s a domanda). La valutazione finale simula i criteri dei concorsi ufficiali.",
            Icons.Default.Timer
        ),
        TutorialStep(
            "Statistiche",
            "Monitora costantemente il tuo Indice di Preparazione per identificare le aree che necessitano di revisione.",
            Icons.Default.BarChart
        ),
        TutorialStep(
            "Impostazioni",
            "Personalizza il tema, abilita la vibrazione o esporta il database.",
            Icons.Default.Settings
        ),
        TutorialStep(
            "Approfondimento",
            "Per dettagli tecnici sulle logiche di valutazione e l'algoritmo SMART, consulta la 'Guida Logiche App' nelle Impostazioni.",
            Icons.Default.Info
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(steps[currentStep].icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary) },
        title = { Text(text = steps[currentStep].title, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = steps[currentStep].description, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${currentStep + 1} di ${steps.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentStep < steps.size - 1) {
                        currentStep++
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(if (currentStep < steps.size - 1) "Avanti" else "Ho capito")
            }
        },
        dismissButton = {
            if (currentStep > 0) {
                TextButton(onClick = { currentStep-- }) {
                    Text("Indietro")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Salta")
                }
            }
        }
    )
}
