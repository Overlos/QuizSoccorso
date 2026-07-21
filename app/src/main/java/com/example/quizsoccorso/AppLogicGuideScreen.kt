package com.example.quizsoccorso

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quizsoccorso.ui.utils.MarkdownUtils

/**
 * Schermata di approfondimento sulle logiche di funzionamento dell'app.
 * Spiega all'utente come vengono calcolate le statistiche e come funziona l'algoritmo SMART.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLogicGuideScreen(onBack: () -> Unit) {
    // Gestione tasto indietro di sistema
    androidx.activity.compose.BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(R.string.guide_title)) },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GuideSection(
                title = androidx.compose.ui.res.stringResource(R.string.guide_alg_smart_title),
                content = androidx.compose.ui.res.stringResource(R.string.guide_alg_smart_content)
            )

            GuideSection(
                title = androidx.compose.ui.res.stringResource(R.string.guide_prep_index_title),
                content = androidx.compose.ui.res.stringResource(R.string.guide_prep_index_content)
            )

            GuideSection(
                title = androidx.compose.ui.res.stringResource(R.string.guide_thresholds_title),
                content = androidx.compose.ui.res.stringResource(R.string.guide_thresholds_content)
            )

            GuideSection(
                title = androidx.compose.ui.res.stringResource(R.string.guide_dynamic_diff_title),
                content = androidx.compose.ui.res.stringResource(R.string.guide_dynamic_diff_content)
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(androidx.compose.ui.res.stringResource(R.string.guide_understood))
            }
        }
    }
}

@Composable
private fun GuideSection(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = MarkdownUtils.parseMarkdown(content),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
