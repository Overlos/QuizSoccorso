package com.example.quizsoccorso

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Schermata di login per l'accesso all'area amministrativa.
 */
@Composable
fun AdminLoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Area Riservata", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                error = false
            },
            label = { Text("Password Admin") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = error,
            modifier = Modifier.fillMaxWidth()
        )
        
        if (error) {
            Text("Password errata", color = MaterialTheme.colorScheme.error)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (password == "admin") {
                    onLoginSuccess()
                } else {
                    error = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Accedi")
        }
        
        TextButton(onClick = onBack) {
            Text("Annulla")
        }
    }
}

/**
 * Schermata dell'Editor Domande con navigazione gerarchica Tag -> Capitoli -> Domande.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionEditorScreen(
    questions: List<QuizQuestion>,
    onSaveQuestion: (QuizQuestion) -> Unit,
    onDeleteQuestion: (Int) -> Unit,
    onBack: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf<QuizQuestion?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Int?>(null) }
    
    // Stato per la navigazione
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }

    // Gestione tasto indietro di sistema specifica per l'editor
    BackHandler(enabled = selectedTag != null || searchQuery.isNotEmpty()) {
        if (searchQuery.isNotEmpty()) searchQuery = ""
        else if (selectedCategory != null) selectedCategory = null
        else selectedTag = null
    }

    // Raggruppamento dati e filtraggio per ricerca
    val filteredBySearch = remember(questions, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else questions.filter { 
            it.question.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
        }
    }

    val questionsByTag = remember(questions) {
        val map = mutableMapOf<String, MutableList<QuizQuestion>>()
        questions.forEach { q ->
            if (q.tags.isEmpty()) {
                map.getOrPut("Senza Tag") { mutableListOf() }.add(q)
            } else {
                q.tags.forEach { tag ->
                    map.getOrPut(tag) { mutableListOf() }.add(q)
                }
            }
        }
        map.toSortedMap()
    }

    val categoriesInSelectedTag = remember(selectedTag, questionsByTag) {
        selectedTag?.let { tag ->
            questionsByTag[tag]?.groupBy { it.category }?.keys?.sorted()
        } ?: emptyList()
    }

    val filteredQuestions = remember(selectedTag, selectedCategory, questionsByTag) {
        if (selectedTag != null && selectedCategory != null) {
            questionsByTag[selectedTag]?.filter { it.category == selectedCategory } ?: emptyList()
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Editor Domande", 
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (selectedTag != null && searchQuery.isEmpty()) {
                            Text(
                                text = "${selectedTag}${if (selectedCategory != null) " > $selectedCategory" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "Ricerca: $searchQuery",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (searchQuery.isNotEmpty()) searchQuery = ""
                        else if (selectedCategory != null) selectedCategory = null
                        else if (selectedTag != null) selectedTag = null
                        else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showEditDialog = QuizQuestion(
                            id = 0,
                            question = "",
                            category = selectedCategory ?: "",
                            tags = if (selectedTag != null) listOf(selectedTag!!) else emptyList(),
                            difficulty = 3,
                            answers = listOf("", "", "", ""),
                            correct = "",
                            explanation = "",
                            source = ""
                        )
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Aggiungi")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            // Barra di ricerca
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Cerca per testo, capitolo o tag...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancella")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            if (searchQuery.isNotEmpty()) {
                // Vista Ricerca: Elenco piatto dei risultati
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Risultati trovati: ${filteredBySearch.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    items(filteredBySearch) { question ->
                        QuestionListItem(
                            question = question,
                            onEdit = { showEditDialog = question },
                            onDelete = { showDeleteConfirm = question.id }
                        )
                    }
                }
            } else if (selectedTag == null) {
                // Vista 1: Elenco dei Tag
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("Seleziona un Tag", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    items(questionsByTag.keys.toList()) { tag ->
                        val count = questionsByTag[tag]?.size ?: 0
                        Button(
                            onClick = { selectedTag = tag },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(tag)
                                Text("($count)", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            } else if (selectedCategory == null) {
                // Vista 2: Elenco dei Capitoli nel Tag selezionato
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("Capitoli in: $selectedTag", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    items(categoriesInSelectedTag) { category ->
                        val count = questionsByTag[selectedTag]?.count { it.category == category } ?: 0
                        OutlinedButton(
                            onClick = { selectedCategory = category },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(category)
                                Text("($count)", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            } else {
                // Vista 3: Elenco delle domande nel Capitolo selezionato
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Domande: $selectedCategory", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    items(filteredQuestions) { question ->
                        QuestionListItem(
                            question = question,
                            onEdit = { showEditDialog = question },
                            onDelete = { showDeleteConfirm = question.id }
                        )
                    }
                }
            }
        }
    }

    showEditDialog?.let { q ->
        EditQuestionDialog(
            question = q,
            onSave = { 
                onSaveQuestion(it)
                showEditDialog = null
            },
            onDismiss = { showEditDialog = null }
        )
    }

    showDeleteConfirm?.let { id ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Conferma eliminazione") },
            text = { Text("Sei sicuro di voler eliminare questa domanda definitivamente?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteQuestion(id)
                    showDeleteConfirm = null
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { 
                    Text("Elimina") 
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Annulla") }
            }
        )
    }
}

@Composable
fun QuestionListItem(
    question: QuizQuestion,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val useColumnForHeader = MaterialTheme.typography.titleMedium.fontSize.value > 22

            if (useColumnForHeader) {
                Column {
                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifica", tint = MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = question.question,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifica", tint = MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ID: ${question.id} | Difficoltà: ${question.difficulty}/5",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

/**
 * Dialog con validazione obbligatoria dei campi.
 */
@Composable
fun EditQuestionDialog(
    question: QuizQuestion,
    onSave: (QuizQuestion) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(question.question) }
    var category by remember { mutableStateOf(question.category) }
    var answers by remember { mutableStateOf(question.answers.toMutableList()) }
    
    val initialCorrectIndex = answers.indexOf(question.correct).takeIf { it != -1 } ?: -1
    var selectedCorrectIndex by remember { mutableStateOf(initialCorrectIndex) }
    
    var explanation by remember { mutableStateOf(question.explanation) }
    var source by remember { mutableStateOf(question.source) }
    var tagsStr by remember { mutableStateOf(question.tags.joinToString(", ")) }
    var difficulty by remember { mutableStateOf(question.difficulty.toString()) }

    // Validazione
    val isFormValid = text.isNotBlank() && 
                      category.isNotBlank() && 
                      answers.all { it.isNotBlank() } && 
                      selectedCorrectIndex != -1 && 
                      tagsStr.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (question.id == 0) "Nuova Domanda" else "Modifica Domanda") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = text, 
                    onValueChange = { text = it }, 
                    label = { Text("Domanda (Obbligatoria)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    isError = text.isBlank()
                )
                
                OutlinedTextField(
                    value = category, 
                    onValueChange = { category = it }, 
                    label = { Text("Capitolo (Obbligatorio)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    isError = category.isBlank()
                )
                
                OutlinedTextField(
                    value = tagsStr, 
                    onValueChange = { tagsStr = it }, 
                    label = { Text("Tag (es: SSE, Autisti - Obbligatorio)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    isError = tagsStr.isBlank()
                )
                
                OutlinedTextField(
                    value = difficulty, 
                    onValueChange = { difficulty = it }, 
                    label = { Text("Difficoltà (1-5)") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                    modifier = Modifier.fillMaxWidth()
                )
                
                HorizontalDivider()
                Text("Risposte (Scegli la corretta):", style = MaterialTheme.typography.titleSmall)
                
                Column(Modifier.selectableGroup()) {
                    answers.forEachIndexed { index, ans ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (index == selectedCorrectIndex),
                                    onClick = { selectedCorrectIndex = index },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (index == selectedCorrectIndex),
                                onClick = null
                            )
                            OutlinedTextField(
                                value = ans,
                                onValueChange = {
                                    val newList = answers.toMutableList()
                                    newList[index] = it
                                    answers = newList
                                },
                                label = { Text("Risposta ${index + 1}") },
                                modifier = Modifier.weight(1f).padding(start = 8.dp),
                                isError = ans.isBlank()
                            )
                        }
                    }
                }
                
                if (selectedCorrectIndex == -1) {
                    Text("Seleziona una risposta corretta!", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
                
                HorizontalDivider()
                
                OutlinedTextField(value = explanation, onValueChange = { explanation = it }, label = { Text("Spiegazione") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text("Fonte") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        onSave(question.copy(
                            question = text,
                            category = category,
                            tags = tagsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            difficulty = difficulty.toIntOrNull() ?: 3,
                            answers = answers,
                            correct = answers[selectedCorrectIndex],
                            explanation = explanation,
                            source = source
                        ))
                    }
                },
                enabled = isFormValid
            ) { Text("Salva") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}
