package com.example.quizsoccorso

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.quizsoccorso.ui.utils.MarkdownText

/**
 * Finestra di dialogo obbligatoria che presenta il disclaimer legale.
 * Viene mostrata automaticamente al primo avvio finché non viene accettata.
 * Può essere richiamata anche dalle Impostazioni per sola consultazione.
 */
@Composable
fun DisclaimerDialog(
    onAccept: () -> Unit,             // Azione da eseguire quando l'utente accetta i termini
    showAcceptButton: Boolean = true, // Se mostrare il tasto di accettazione o solo quello di chiusura
    onDismiss: () -> Unit = {}        // Azione alla chiusura (se il tasto accettazione non è presente)
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !showAcceptButton,
            dismissOnClickOutside = !showAcceptButton,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Disclaimer Legale",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    MarkdownText(
                        text = """
                            # Disclaimer

                            La presente applicazione è stata sviluppata esclusivamente come **strumento di supporto allo studio e al ripasso personale**. Non costituisce in alcun modo un programma didattico ufficiale, né sostituisce la formazione teorica e pratica prevista dai corsi per soccorritori.

                            I contenuti dell'app sono forniti a solo scopo educativo e potrebbero contenere errori, omissioni o informazioni non aggiornate. Pur essendo stata prestata la massima attenzione nella loro preparazione, **non è possibile garantirne la completezza, l'accuratezza o la conformità alle più recenti linee guida e procedure operative**.

                            L'utilizzo dell'app **non costituisce consulenza medica, sanitaria o professionale** e non deve essere impiegato per assumere decisioni cliniche, assistenziali o operative durante interventi di soccorso.

                            Le indicazioni fornite da **istruttori, formatori, responsabili della formazione, protocolli ufficiali, linee guida vigenti e documentazione dell'ente di appartenenza prevalgono sempre** sui contenuti presenti nell'applicazione.

                            L'autore non è affiliato, autorizzato o rappresenta alcun ente di formazione, organizzazione di soccorso o istituzione sanitaria, salvo diversa ed esplicita indicazione.

                            L'utente è l'unico responsabile della verifica delle informazioni e del proprio percorso formativo. L'utilizzo dell'app implica l'accettazione che essa rappresenta esclusivamente un **coadiuvante allo studio**, senza alcuna garanzia di superamento dell'esame o di idoneità professionale.

                            L'autore declina ogni responsabilità per eventuali danni, diretti o indiretti, derivanti dall'utilizzo dell'applicazione o dall'affidamento ai suoi contenuti.
                        """.trimIndent()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (showAcceptButton) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ho letto e accetto le condizioni")
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Chiudi")
                    }
                }
            }
        }
    }
}
