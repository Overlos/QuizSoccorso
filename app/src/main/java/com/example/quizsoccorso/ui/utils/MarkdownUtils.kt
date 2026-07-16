package com.example.quizsoccorso.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * Utility per il parsing di semplice markdown testuale.
 */
object MarkdownUtils {
    /**
     * Converte le occorrenze di **testo** in testo grassetto (bold) usando AnnotatedString.
     * Gestisce anche righe che iniziano con "# " come intestazioni (headers).
     */
    fun parseMarkdown(text: String): AnnotatedString {
        return buildAnnotatedString {
            val lines = text.lines()
            lines.forEachIndexed { index, line ->
                if (line.trim().startsWith("# ")) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                        append(line.trim().substring(2))
                    }
                } else {
                    var currentText = line
                    while (currentText.contains("**")) {
                        val start = currentText.indexOf("**")
                        append(currentText.substring(0, start))
                        val rest = currentText.substring(start + 2)
                        val end = rest.indexOf("**")
                        if (end != -1) {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(rest.substring(0, end))
                            }
                            currentText = rest.substring(end + 2)
                        } else {
                            append("**")
                            currentText = rest
                        }
                    }
                    append(currentText)
                }
                if (index < lines.size - 1) append("\n")
            }
        }
    }
}

/**
 * Composable che renderizza testo Markdown semplice.
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    Text(
        text = MarkdownUtils.parseMarkdown(text),
        modifier = modifier,
        style = style
    )
}
