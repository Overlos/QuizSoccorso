package com.example.quizsoccorso

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Schermata di configurazione per la Simulazione d'Esame.
 * Permette di scegliere il numero di domande e calcola tempo e soglie di conseguenza.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamConfigScreen(
    onStartExam: (Int) -> Unit,
    onBack: () -> Unit
) {
    // Opzioni disponibili per il numero di domande
    val options = listOf(10, 15, 20, 30, 40, 50, 60)
    var selectedCount by remember { mutableIntStateOf(30) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Configura Esame", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Il tempo e la soglia di sbarramento verranno calcolati proporzionalmente in base al numero di domande scelto.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text("Numero di domande:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        options.forEach { count ->
            val isOfficial = count == 30
            FilterChip(
                selected = selectedCount == count,
                onClick = { selectedCount = count },
                label = { 
                    Text(if (isOfficial) "$count (Ufficiale ⭐)" else "$count") 
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onStartExam(selectedCount) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Inizia Esame")
        }

        TextButton(onClick = onBack) {
            Text("Annulla")
        }
    }
}

/**
 * Dialog per scegliere il numero di domande in Modalità Studio quando si sceglie "Quiz Misto".
 */
@Composable
fun StudyConfigDialog(
    onConfirm: (Int?) -> Unit, // null significa "Tutte le domande (Full)"
    onDismiss: () -> Unit
) {
    val options = listOf(10, 20, 30, 50, 100)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Numero di domande") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text("Quante domande vuoi affrontare in questa sessione di studio misto?")
                Spacer(modifier = Modifier.height(8.dp))
                
                options.forEach { count ->
                    OutlinedButton(
                        onClick = { onConfirm(count) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("$count domande")
                    }
                }
                
                Button(
                    onClick = { onConfirm(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutte le domande (Full)")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}
