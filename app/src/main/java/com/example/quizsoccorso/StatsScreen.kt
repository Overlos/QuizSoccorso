package com.example.quizsoccorso

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Schermata delle statistiche storiche.
 * Permette di vedere la precisione per capitolo e scendere nel dettaglio delle singole domande.
 */
@Composable
fun StatsScreen(
    categoryStats: List<CategoryStat>,
    prepIndex: PreparationIndex,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onResetStats: () -> Unit,
    getQuestionStats: (String, String) -> List<Pair<QuizQuestion, QuestionStat>>
) {
    var showResetConfirm by remember { mutableStateOf(false) }
    
    // Stato per la navigazione interna (null = elenco capitoli, Pair = capitolo e tag selezionati)
    var selectedDetail by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Gestione tasto indietro di sistema
    BackHandler(enabled = selectedDetail != null) {
        selectedDetail = null
    }

    val totalAnswered = categoryStats.sumOf { it.answeredCount }
    val totalQuestions = categoryStats.sumOf { it.totalCount }

    // Raggruppamento delle statistiche per macro-sezioni basate sui Tag
    val groupedStats = remember(categoryStats) {
        val activeSections = defaultQuizSections.filter { it.tags != null }
        val result = LinkedHashMap<String, MutableList<CategoryStat>>()
        
        categoryStats.forEach { stat ->
            val tag = stat.tags.firstOrNull() ?: "Senza Tag"
            val section = activeSections.find { s -> s.tags?.contains(tag) == true }
            val sectionName = section?.label ?: "Capitoli Generali"
            result.getOrPut(sectionName) { mutableListOf() }.add(stat)
        }
        result
    }

    if (showResetConfirm) {
        ResetConfirmDialog(
            onConfirm = {
                showResetConfirm = false
                onResetStats()
            },
            onDismiss = { showResetConfirm = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        if (selectedDetail == null) {
            // VISTA 1: Elenco Capitoli raggruppati
            StatsOverview(
                totalAnswered = totalAnswered,
                totalQuestions = totalQuestions,
                groupedStats = groupedStats,
                prepIndex = prepIndex,
                onCategoryClick = { category, tag -> selectedDetail = category to tag },
                onBack = onBack,
                onHome = onHome,
                onResetClick = { showResetConfirm = true }
            )
        } else {
            // VISTA 2: Dettaglio domande per il capitolo e tag selezionati
            val (category, tag) = selectedDetail!!
            val details = getQuestionStats(category, tag)
            QuestionStatsDetail(
                categoryName = category,
                tagName = tag,
                details = details,
                onBack = { selectedDetail = null }
            )
        }
    }
}

@Composable
private fun StatsOverview(
    totalAnswered: Int,
    totalQuestions: Int,
    groupedStats: Map<String, List<CategoryStat>>,
    prepIndex: PreparationIndex,
    onCategoryClick: (String, String) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onResetClick: () -> Unit
) {
    Text(text = androidx.compose.ui.res.stringResource(R.string.stats_history), style = MaterialTheme.typography.headlineMedium)

    Spacer(modifier = Modifier.height(20.dp))

    // Preparation Index Card - Redesigned
    val indicatorColor = when {
        prepIndex.score >= 95 -> Color(0xFF1B5E20)
        prepIndex.score >= 90 -> Color(0xFF388E3C)
        prepIndex.score >= 85 -> Color(0xFFFBC02D)
        else -> Color(0xFFC62828)
    }

    val backgroundColor = when {
        prepIndex.score >= 95 -> Color(0xFFE8F5E9)
        prepIndex.score >= 90 -> Color(0xFFF1F8E9)
        prepIndex.score >= 85 -> Color(0xFFFFFDE7)
        else -> Color(0xFFFFF1F0)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.current_level),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = prepIndex.level.uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = indicatorColor
                    )
                }

                Icon(
                    imageVector = when {
                        prepIndex.score >= 95 -> Icons.Default.EmojiEvents
                        prepIndex.score >= 90 -> Icons.Default.Star
                        prepIndex.score >= 85 -> Icons.Default.CheckCircle
                        else -> Icons.Default.ErrorOutline
                    },
                    contentDescription = null,
                    tint = indicatorColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score with Progress Bar
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${prepIndex.score}",
                    style = MaterialTheme.typography.displayMedium,
                    color = indicatorColor,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.score_max_suffix),
                    style = MaterialTheme.typography.titleMedium,
                    color = indicatorColor.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )
            }

            LinearProgressIndicator(
                progress = { prepIndex.score.toFloat() / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = indicatorColor,
                trackColor = indicatorColor.copy(alpha = 0.2f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Color.White.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = prepIndex.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.answered_stats, totalAnswered, totalQuestions),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (groupedStats.isEmpty()) {
        Text(text = androidx.compose.ui.res.stringResource(R.string.no_data), style = MaterialTheme.typography.bodyMedium)
    } else {
        groupedStats.forEach { (sectionName, stats) ->
            Text(
                text = sectionName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            stats.forEach { stat ->
                val tag = stat.tags.firstOrNull() ?: androidx.compose.ui.res.stringResource(R.string.no_tag)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onCategoryClick(stat.category, tag) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stat.category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (stat.precisionPercent >= 0) {
                                Surface(
                                    color = when {
                                        stat.precisionPercent >= 95 -> Color(0xFFE8F5E9)
                                        stat.precisionPercent >= 90 -> Color(0xFFF1F8E9)
                                        stat.precisionPercent >= 85 -> Color(0xFFFFFDE7)
                                        else -> Color(0xFFFFF1F0)
                                    },
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        text = "${stat.precisionPercent}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            stat.precisionPercent >= 95 -> Color(0xFF1B5E20)
                                            stat.precisionPercent >= 90 -> Color(0xFF388E3C)
                                            stat.precisionPercent >= 85 -> Color(0xFFFBC02D)
                                            else -> Color(0xFFC62828)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (stat.precisionPercent >= 0) {
                            LinearProgressIndicator(
                                progress = { stat.precisionPercent.toFloat() / 100f },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.category_stats_format, stat.answeredCount, stat.totalCount, precisionLabel(stat.precisionPercent)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.questions_count_not_attempted, stat.totalCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.tag_format, tag),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    StatsFooterButtons(onBack = onBack, onResetClick = onResetClick, onHome = onHome)
}

@Composable
private fun QuestionStatsDetail(
    categoryName: String,
    tagName: String,
    details: List<Pair<QuizQuestion, QuestionStat>>,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = androidx.compose.ui.res.stringResource(R.string.chapter_detail, tagName), style = MaterialTheme.typography.headlineSmall)
        Text(text = categoryName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        
        Spacer(modifier = Modifier.height(16.dp))

        // Usiamo una LazyColumn interna per gestire potenzialmente centinaia di domande
        // Nota: Poiché la StatsScreen esterna è già scrollable, dobbiamo stare attenti.
        // Soluzione migliore: La StatsScreen non dovrebbe essere interamente scrollable se contiene liste lunghe.
        // Ma per ora, manteniamo la coerenza e miglioriamo la visualizzazione.
        
        details.forEach { (question, stat) ->
            QuestionStatCard(question = question, stat = stat)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(androidx.compose.ui.res.stringResource(R.string.back_to_chapters))
        }
    }
}

@Composable
private fun QuestionStatCard(question: QuizQuestion, stat: QuestionStat) {
    val precision = if (stat.attempts > 0) (stat.correct * 100 / stat.attempts) else -1
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                stat.attempts == 0 -> MaterialTheme.colorScheme.surfaceVariant
                precision < 85 -> Color(0xFFFFF1F0)
                precision < 90 -> Color(0xFFFFFDE7)
                precision < 95 -> Color(0xFFF1F8E9)
                else -> Color(0xFFE8F5E9)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Risposta corretta
            Surface(
                color = Color(0xFFE8F5E9),
                shape = MaterialTheme.shapes.small
            ) {
                Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.correct_answer_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = question.correct,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.explanation_format, question.explanation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            
            val useColumnForFooter = MaterialTheme.typography.labelMedium.fontSize.value > 18

            if (useColumnForFooter) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.attempts_format, stat.attempts),
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (stat.attempts > 0) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.correct_format, stat.correct),
                                style = MaterialTheme.typography.labelSmall
                            )
                            if (stat.userDifficulty > 0) {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(R.string.custom_difficulty_format, stat.userDifficulty),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    if (precision >= 0) {
                        Column {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.precision_format, precision),
                                style = MaterialTheme.typography.titleMedium,
                                color = when {
                                    precision < 85 -> Color(0xFFC62828)
                                    precision < 90 -> Color(0xFFFBC02D)
                                    precision < 95 -> Color(0xFF388E3C)
                                    else -> Color(0xFF1B5E20)
                                },
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = precisionLabel(precision),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.never_attempted),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.attempts_format, stat.attempts),
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (stat.attempts > 0) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.correct_format, stat.correct),
                                style = MaterialTheme.typography.labelSmall
                            )
                            if (stat.userDifficulty > 0) {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(R.string.custom_difficulty_format, stat.userDifficulty),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    if (precision >= 0) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.precision_format, precision),
                                style = MaterialTheme.typography.titleMedium,
                                color = when {
                                    precision < 85 -> Color(0xFFC62828)
                                    precision < 90 -> Color(0xFFFBC02D)
                                    precision < 95 -> Color(0xFF388E3C)
                                    else -> Color(0xFF1B5E20)
                                },
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = precisionLabel(precision),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.never_attempted),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsFooterButtons(onBack: () -> Unit, onResetClick: () -> Unit, onHome: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onBack) { Text(androidx.compose.ui.res.stringResource(R.string.back_arrow)) }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onResetClick) { Text(androidx.compose.ui.res.stringResource(R.string.reset_stats)) }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onHome) { Text(androidx.compose.ui.res.stringResource(R.string.home_btn)) }
    }
}

@Composable
private fun ResetConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(androidx.compose.ui.res.stringResource(R.string.reset_stats)) },
        text = { Text(androidx.compose.ui.res.stringResource(R.string.reset_stats_message)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(androidx.compose.ui.res.stringResource(R.string.reset)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(androidx.compose.ui.res.stringResource(R.string.cancel)) } }
    )
}
