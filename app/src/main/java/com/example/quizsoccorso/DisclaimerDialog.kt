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
                    text = androidx.compose.ui.res.stringResource(R.string.disclaimer_title_full),
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
                        text = androidx.compose.ui.res.stringResource(R.string.disclaimer_content)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (showAcceptButton) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(androidx.compose.ui.res.stringResource(R.string.disclaimer_accept))
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(androidx.compose.ui.res.stringResource(R.string.close))
                    }
                }
            }
        }
    }
}
