package com.example.quizsoccorso

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
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
    onEditorClick: () -> Unit = {},
    isAltered: Boolean = false,
    onAlteredClick: () -> Unit = {},
    isLoading: Boolean = false,
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
            IconButton(onClick = onTutorialClick) {
                Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Tutorial")
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
            text = androidx.compose.ui.res.stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = androidx.compose.ui.res.stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(36.dp))

        // SELETTORE DELLA SEZIONE (SSE, Autisti, Misto)
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.select_section),
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
                    text = androidx.compose.ui.res.stringResource(R.string.mode_studio_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.mode_studio_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onModeSelected(QuizMode.STUDIO) }
                ) {
                    Text(androidx.compose.ui.res.stringResource(R.string.start_study))
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
                    text = androidx.compose.ui.res.stringResource(R.string.mode_smart_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.mode_smart_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    onClick = { onModeSelected(QuizMode.SMART) }
                ) {
                    Text(androidx.compose.ui.res.stringResource(R.string.start_smart))
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
                    text = androidx.compose.ui.res.stringResource(R.string.mode_exam_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.mode_exam_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onModeSelected(QuizMode.ESAME) }
                ) {
                    Text(androidx.compose.ui.res.stringResource(R.string.start_exam))
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
            Text(androidx.compose.ui.res.stringResource(R.string.stats), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Editor delle domande (permette modifiche al database locale)
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
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            TextButton(
                onClick = onEditorClick
            ) {
                Text(androidx.compose.ui.res.stringResource(R.string.editor), style = MaterialTheme.typography.labelSmall)
            }
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
