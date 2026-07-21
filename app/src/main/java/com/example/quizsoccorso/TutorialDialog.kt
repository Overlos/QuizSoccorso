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
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_1_title),
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_1_desc),
            Icons.Default.Celebration
        ),
        TutorialStep(
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_2_title),
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_2_desc),
            Icons.Default.MedicalServices
        ),
        TutorialStep(
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_3_title),
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_3_desc),
            Icons.Default.School
        ),
        TutorialStep(
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_4_title),
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_4_desc),
            Icons.Default.Psychology
        ),
        TutorialStep(
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_5_title),
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_5_desc),
            Icons.Default.Timer
        ),
        TutorialStep(
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_6_title),
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_6_desc),
            Icons.Default.BarChart
        ),
        TutorialStep(
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_7_title),
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_7_desc),
            Icons.Default.Settings
        ),
        TutorialStep(
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_8_title),
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_8_desc),
            Icons.Default.BugReport
        ),
        TutorialStep(
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_9_title),
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_9_desc),
            Icons.Default.Warning
        ),
        TutorialStep(
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_10_title),
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_10_desc),
            Icons.Default.Edit
        ),
        TutorialStep(
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_11_title),
            androidx.compose.ui.res.stringResource(R.string.tutorial_step_11_desc),
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
                    text = androidx.compose.ui.res.stringResource(R.string.tutorial_progress_format, currentStep + 1, steps.size),
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
                Text(androidx.compose.ui.res.stringResource(if (currentStep < steps.size - 1) R.string.next else R.string.guide_understood))
            }
        },
        dismissButton = {
            if (currentStep > 0) {
                TextButton(onClick = { currentStep-- }) {
                    Text(androidx.compose.ui.res.stringResource(R.string.back))
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(androidx.compose.ui.res.stringResource(R.string.skip))
                }
            }
        }
    )
}
