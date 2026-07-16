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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logiche e Funzionamento") },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GuideSection(
                title = "🧠 Algoritmo Quiz SMART",
                content = """
                    La modalità SMART utilizza un algoritmo di priorità per selezionare i quesiti più utili per il tuo percorso di apprendimento.
                    
                    Viene assegnato un punteggio a ogni domanda basato su:
                    1. **Precisione Storica**: Le lacune conoscitive identificate aumentano la priorità.
                    2. **Difficoltà Soggettiva**: I quesiti che hai valutato come complessi compaiono con maggiore frequenza.
                    3. **Spaced Repetition (Ripetizione Dilazionata)**: Quando rispondi correttamente a un quesito in modo costante, l'app dilata i tempi prima di riproporlo, favorendo la memoria a lungo termine.
                """.trimIndent()
            )

            GuideSection(
                title = "📊 Indice di Preparazione",
                content = """
                    L'indice globale (0-100) non è una semplice media, ma una valutazione ponderata della tua competenza:
                    
                    - **Precisione (40%)**: La percentuale complessiva di risposte corrette.
                    - **Recenza (30%)**: Le performance degli ultimi 7 giorni riflettono lo stato attuale della tua preparazione.
                    - **Padronanza (20%)**: Indica la quota di quesiti a cui hai fornito una risposta corretta in almeno 3 occasioni consecutive.
                    - **Consistenza (10%)**: Valuta l'uniformità dello studio tra i diversi capitoli disponibili.
                """.trimIndent()
            )

            GuideSection(
                title = "🎯 Soglie di Valutazione",
                content = """
                    Per garantirti una preparazione d'eccellenza, abbiamo definito soglie di merito rigorose:
                    
                    - **Ottimo (>= 95%)**: Dimostrazione di padronanza quasi totale della materia.
                    - **Buono (>= 90%)**: Conoscenza solida e affidabile.
                    - **Sufficiente (>= 85%)**: Requisito minimo per considerarsi pronti all'esame.
                    - **Da ripassare (< 85%)**: Segnala la necessità di approfondire ulteriormente gli argomenti.
                """.trimIndent()
            )

            GuideSection(
                title = "📈 Difficoltà Dinamica",
                content = """
                    L'applicazione monitora costantemente le tue risposte. In caso di errore, la domanda viene considerata più critica per il tuo studio. Al contrario, quando dimostri padronanza della risposta, l'algoritmo ne riduce la priorità.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Ho capito")
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
