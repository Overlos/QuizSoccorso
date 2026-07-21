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
                title = { Text(androidx.compose.ui.res.stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(R.string.back))
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
                    text = androidx.compose.ui.res.stringResource(R.string.app_theme),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(Modifier.selectableGroup()) {
                    ThemeOption(
                        label = androidx.compose.ui.res.stringResource(R.string.theme_system),
                        selected = state.theme == AppTheme.SYSTEM,
                        onClick = { onThemeChanged(AppTheme.SYSTEM) }
                    )
                    ThemeOption(
                        label = androidx.compose.ui.res.stringResource(R.string.theme_light),
                        selected = state.theme == AppTheme.LIGHT,
                        onClick = { onThemeChanged(AppTheme.LIGHT) }
                    )
                    ThemeOption(
                        label = androidx.compose.ui.res.stringResource(R.string.theme_light_lively),
                        selected = state.theme == AppTheme.LIGHT_LIVELY,
                        onClick = { onThemeChanged(AppTheme.LIGHT_LIVELY) }
                    )
                    ThemeOption(
                        label = androidx.compose.ui.res.stringResource(R.string.theme_dark),
                        selected = state.theme == AppTheme.DARK,
                        onClick = { onThemeChanged(AppTheme.DARK) }
                    )
                    ThemeOption(
                        label = androidx.compose.ui.res.stringResource(R.string.theme_dark_lively),
                        selected = state.theme == AppTheme.DARK_LIVELY,
                        onClick = { onThemeChanged(AppTheme.DARK_LIVELY) }
                    )
                    ThemeOption(
                        label = androidx.compose.ui.res.stringResource(R.string.theme_accessible),
                        selected = state.theme == AppTheme.ACCESSIBLE,
                        onClick = { onThemeChanged(AppTheme.ACCESSIBLE) }
                    )
                    ThemeOption(
                        label = androidx.compose.ui.res.stringResource(R.string.theme_oled),
                        selected = state.theme == AppTheme.OLED,
                        onClick = { onThemeChanged(AppTheme.OLED) }
                    )
                    ThemeOption(
                        label = androidx.compose.ui.res.stringResource(R.string.theme_reading),
                        selected = state.theme == AppTheme.READING,
                        onClick = { onThemeChanged(AppTheme.READING) }
                    )
                }
            }

            HorizontalDivider()

            // DIMENSIONE TESTO
            Column {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.font_size_label, state.fontSizeMultiplier),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.font_size_desc),
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
                            text = androidx.compose.ui.res.stringResource(R.string.haptic_feedback),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.haptic_desc),
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
                            text = androidx.compose.ui.res.stringResource(R.string.haptic_feedback),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.haptic_desc),
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
                    text = androidx.compose.ui.res.stringResource(R.string.doc_and_logic),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onShowGuide,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(androidx.compose.ui.res.stringResource(R.string.guide_logics))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = onShowDisclaimer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(androidx.compose.ui.res.stringResource(R.string.legal_notes))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onOpenPrivacy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(androidx.compose.ui.res.stringResource(R.string.privacy_policy))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onViewCode,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(androidx.compose.ui.res.stringResource(R.string.source_code))
                }
            }

            HorizontalDivider()

            // DATI E DATABASE: Gestione del database JSON
            Column {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.data_and_db),
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
                        Text(androidx.compose.ui.res.stringResource(R.string.import_json), style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = onExportDatabase,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(androidx.compose.ui.res.stringResource(R.string.export_json_btn), style = MaterialTheme.typography.labelSmall)
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
                        Text(androidx.compose.ui.res.stringResource(R.string.export_github), style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = onShareModifications,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(androidx.compose.ui.res.stringResource(R.string.export_email), style = MaterialTheme.typography.labelSmall)
                    }
                }

                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.export_info),
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
