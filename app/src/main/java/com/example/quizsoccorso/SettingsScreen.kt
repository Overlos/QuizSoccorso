package com.example.quizsoccorso

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Schermata delle impostazioni dell'applicazione.
 * Permette di gestire il tema, il feedback aptico, leggere la documentazione ed esportare i dati.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onThemeChanged: (AppTheme) -> Unit,
    onFontSizeChanged: (Float) -> Unit,
    onHapticChanged: (Boolean) -> Unit,
    onExportDatabase: () -> Unit,
    onImportDatabase: () -> Unit,
    onExportGitHub: () -> Unit,
    onShareModifications: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onViewCode: () -> Unit,
    onShowDisclaimer: () -> Unit,
    onShowGuide: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sezione TEMA: Consente di scegliere tra Chiaro, Scuro o Sistema
            Column {
                Text(
                    text = "Tema Applicazione",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(Modifier.selectableGroup()) {
                    ThemeOption(
                        label = "Sistema (Predefinito)",
                        selected = state.theme == AppTheme.SYSTEM,
                        onClick = { onThemeChanged(AppTheme.SYSTEM) }
                    )
                    ThemeOption(
                        label = "Chiaro (Semplice)",
                        selected = state.theme == AppTheme.LIGHT,
                        onClick = { onThemeChanged(AppTheme.LIGHT) }
                    )
                    ThemeOption(
                        label = "Chiaro Lively",
                        selected = state.theme == AppTheme.LIGHT_LIVELY,
                        onClick = { onThemeChanged(AppTheme.LIGHT_LIVELY) }
                    )
                    ThemeOption(
                        label = "Scuro (Semplice)",
                        selected = state.theme == AppTheme.DARK,
                        onClick = { onThemeChanged(AppTheme.DARK) }
                    )
                    ThemeOption(
                        label = "Scuro Lively",
                        selected = state.theme == AppTheme.DARK_LIVELY,
                        onClick = { onThemeChanged(AppTheme.DARK_LIVELY) }
                    )
                    ThemeOption(
                        label = "Accessibile (Daltonico)",
                        selected = state.theme == AppTheme.ACCESSIBLE,
                        onClick = { onThemeChanged(AppTheme.ACCESSIBLE) }
                    )
                    ThemeOption(
                        label = "OLED (Nero Assoluto)",
                        selected = state.theme == AppTheme.OLED,
                        onClick = { onThemeChanged(AppTheme.OLED) }
                    )
                    ThemeOption(
                        label = "Lettura (Seppia)",
                        selected = state.theme == AppTheme.READING,
                        onClick = { onThemeChanged(AppTheme.READING) }
                    )
                }
            }

            HorizontalDivider()

            // DIMENSIONE TESTO
            Column {
                Text(
                    text = "Dimensione Testo (${"%.1f".format(state.fontSizeMultiplier)}x)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Trascina per ingrandire o rimpicciolire i testi dell'app.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = state.fontSizeMultiplier,
                    onValueChange = onFontSizeChanged,
                    valueRange = 0.8f..2.0f,
                    steps = 11, // Incrementi di 0.1
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            HorizontalDivider()

            // FEEDBACK APTICO: Vibrazione durante l'interazione con le risposte
            val useColumnForHaptic = MaterialTheme.typography.titleMedium.fontSize.value > 20
            
            if (useColumnForHaptic) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Feedback Aptico",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Vibrazione alla selezione delle risposte",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = state.hapticEnabled,
                        onCheckedChange = onHapticChanged
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Feedback Aptico",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Vibrazione alla selezione delle risposte",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = state.hapticEnabled,
                        onCheckedChange = onHapticChanged
                    )
                }
            }

            HorizontalDivider()

            // DOCUMENTAZIONE: Link alle logiche dell'app e al disclaimer legale
            Column {
                Text(
                    text = "Documentazione e Logiche",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onShowGuide,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🧠 Guida Logiche App")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = onShowDisclaimer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("⚖ Note Legali")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onOpenPrivacy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Privacy Policy")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onViewCode,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Codice Sorgente (GitHub)")
                }
            }

            HorizontalDivider()

            // DATI E DATABASE: Gestione del database JSON
            Column {
                Text(
                    text = "Dati e Database",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onImportDatabase,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Importa JSON", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = onExportDatabase,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Esporta JSON", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onExportGitHub,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Esporta GitHub", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = onShareModifications,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Esporta Email", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Text(
                    text = "L'esportazione GitHub o Email include solo le tue modifiche per la revisione.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
