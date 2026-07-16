package com.example.quizsoccorso

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sections: List<QuizSection> = defaultQuizSections,
    selectedSection: QuizSection,
    onSectionSelected: (QuizSection) -> Unit,
    onModeSelected: (QuizMode) -> Unit,
    onStatsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onTutorialClick: () -> Unit = {},
    onAdminClick: () -> Unit = {},
    isAltered: Boolean = false,
    onAlteredClick: () -> Unit = {},
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Toolbar superiore con pulsanti per Tutorial e Impostazioni
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isAltered) {
                IconButton(onClick = onAlteredClick) {
                    Icon(
                        Icons.Default.Warning, 
                        contentDescription = "Database Alterato",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            IconButton(onClick = onTutorialClick) {
                Icon(Icons.Default.HelpOutline, contentDescription = "Tutorial")
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Impostazioni")
            }
        }

        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Quiz Soccorso",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Preparazione Soccorritore",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(36.dp))

        // SELETTORE DELLA SEZIONE (SSE, Autisti, Misto)
        Text(
            text = "Seleziona Sezione di Studio",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Usiamo un layout che si adatta alla dimensione del font: 
        // Row se c'è spazio, altrimenti Column per evitare sovrapposizioni.
        val useColumnLayout = MaterialTheme.typography.labelLarge.fontSize.value > 20

        if (useColumnLayout) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                sections.forEach { section ->
                    SectionItem(
                        section = section,
                        isSelected = section.id == selectedSection.id,
                        onClick = { onSectionSelected(section) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                sections.forEach { section ->
                    SectionItem(
                        section = section,
                        isSelected = section.id == selectedSection.id,
                        onClick = { onSectionSelected(section) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // MODALITÀ STUDIO: Correzione istantanea per l'apprendimento mirato.
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "📚 Modalità Studio",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Correzione immediata, spiegazioni e fonti per ogni quesito.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onModeSelected(QuizMode.STUDIO) }
                ) {
                    Text("Inizia Studio")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // MODALITÀ SMART: Algoritmo di priorità sulle domande difficili o non viste.
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🧠 Quiz SMART",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Allenati sulle domande che sbagli più spesso grazie all'AI.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    onClick = { onModeSelected(QuizMode.SMART) }
                ) {
                    Text("Avvia SMART")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // MODALITÀ ESAME: Simulazione ufficiale con timer e correzione finale.
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "📝 Modalità Esame",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Simula un esame reale con timer. Risultati solo alla fine.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onModeSelected(QuizMode.ESAME) }
                ) {
                    Text("Simula Esame")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pulsanti rapidi per Statistiche e Importazione Dati
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onStatsClick,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("📊 Statistiche", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Area Admin per l'editing delle domande (protetta da password)
        TextButton(
            onClick = onAdminClick,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("⚙ Area Admin", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SectionItem(
    section: QuizSection,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (isSelected) 8.dp else 2.dp,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when (section.id) {
                    "sse" -> Icons.Default.MedicalServices
                    "autisti" -> Icons.Default.DirectionsCar
                    else -> Icons.Default.Dashboard
                },
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = section.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
