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
        Text(androidx.compose.ui.res.stringResource(R.string.config_exam), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            androidx.compose.ui.res.stringResource(R.string.config_exam_desc),
            style = MaterialTheme.typography.bodySmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(androidx.compose.ui.res.stringResource(R.string.num_questions), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        options.forEach { count ->
            val isOfficial = count == 30
            FilterChip(
                selected = selectedCount == count,
                onClick = { selectedCount = count },
                label = { 
                    Text(if (isOfficial) androidx.compose.ui.res.stringResource(R.string.official_suffix, count) else "$count") 
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onStartExam(selectedCount) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(androidx.compose.ui.res.stringResource(R.string.start_exam_btn))
        }

        TextButton(onClick = onBack) {
            Text(androidx.compose.ui.res.stringResource(R.string.cancel))
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
        title = { Text(androidx.compose.ui.res.stringResource(R.string.num_questions)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(androidx.compose.ui.res.stringResource(R.string.study_misto_desc))
                Spacer(modifier = Modifier.height(8.dp))
                
                options.forEach { count ->
                    OutlinedButton(
                        onClick = { onConfirm(count) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(androidx.compose.ui.res.stringResource(R.string.num_questions_format, count))
                    }
                }
                
                Button(
                    onClick = { onConfirm(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(androidx.compose.ui.res.stringResource(R.string.all_questions_full))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(androidx.compose.ui.res.stringResource(R.string.cancel))
            }
        }
    )
}
