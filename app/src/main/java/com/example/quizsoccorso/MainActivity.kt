package com.example.quizsoccorso

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.example.quizsoccorso.ui.theme.QuizSoccorsoTheme
import kotlinx.coroutines.launch

/**
 * Activity principale che gestisce la navigazione tra le diverse schermate dell'app.
 */
class MainActivity : ComponentActivity() {

    // Inizializzazione lazy del repository per evitare caricamenti inutili all'avvio
    private val quizRepository by lazy { QuizRepository(applicationContext) }
    private val settingsRepository by lazy { SettingsRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Recupero dei ViewModel utilizzando la Factory per passare le dipendenze
            val quizViewModel: QuizViewModel = viewModel(
                factory = QuizViewModelFactory(quizRepository)
            )
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(settingsRepository)
            )

            // Osservazione dello stato dei ViewModel come StateFlow reattivi
            val settingsState by settingsViewModel.uiState.collectAsState()
            val databaseQuestions by quizViewModel.databaseQuestionsFlow.collectAsState()

            // Applicazione del tema personalizzato basato sulle impostazioni dell'utente
            QuizSoccorsoTheme(
                appTheme = settingsState.theme,
                fontSizeMultiplier = settingsState.fontSizeMultiplier,
                dynamicColor = false
            ) {
                // Gestore per l'apertura del file picker (importazione database JSON)
                val databasePicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    uri?.let { quizViewModel.importDatabase(it) }
                }

                // Stati locali per la navigazione tra le schermate e la selezione dei filtri
                var screen by remember { mutableStateOf("home") }
                var selectedSection by remember {
                    mutableStateOf(defaultQuizSections.first { it.id == "sse" })
                }
                var selectedMode by remember { mutableStateOf(QuizMode.STUDIO) }

                // Stati per la visualizzazione di dialog e tutorial
                var showStudyConfig by remember { mutableStateOf(false) }
                var showTutorial by remember { mutableStateOf(false) }
                var showAdminDisclaimer by remember { mutableStateOf(false) }
                var adminDisclaimerConfirmed by remember { mutableStateOf(false) }
                var showAlteredDisclaimer by remember { mutableStateOf(false) }
                var reportQuestionToProcess by remember { mutableStateOf<QuizQuestion?>(null) }

                // Osservazione dello stato principale della UI del quiz e delle statistiche
                val uiState by quizViewModel.uiState.collectAsState()
                val questionStats by quizViewModel.questionStats.collectAsState()
                val isDatabaseAltered by quizViewModel.isDatabaseAltered.collectAsState()

                // Visualizzazione del disclaimer legale se non è ancora stato accettato (primo avvio)
                if (!settingsState.disclaimerAccepted) {
                    DisclaimerDialog(
                        onAccept = { settingsViewModel.acceptDisclaimer() }
                    )
                }

                // Disclaimer per l'area amministrativa
                if (showAdminDisclaimer) {
                    AdminDisclaimerDialog(
                        onConfirm = {
                            showAdminDisclaimer = false
                            adminDisclaimerConfirmed = true
                            screen = "editor"
                        },
                        onDismiss = { showAdminDisclaimer = false }
                    )
                }

                // Visualizzazione disclaimer database alterato (richiamata dall'icona in home)
                if (showAlteredDisclaimer) {
                    AdminDisclaimerDialog(
                        onConfirm = { showAlteredDisclaimer = false },
                        onDismiss = { showAlteredDisclaimer = false }
                    )
                }

                // Scelta metodo segnalazione errore
                reportQuestionToProcess?.let { question ->
                    AlertDialog(
                        onDismissRequest = { reportQuestionToProcess = null },
                        title = { Text("Segnala Errore") },
                        text = { Text("Come vuoi segnalare l'errore per questa domanda?") },
                        confirmButton = {
                            TextButton(onClick = {
                                openGitHubIssues(question)
                                reportQuestionToProcess = null
                            }) {
                                Text("GitHub")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                sendErrorEmail(question)
                                reportQuestionToProcess = null
                            }) {
                                Text("Email")
                            }
                        }
                    )
                }

                // Gestione del tasto "Indietro" di sistema per la navigazione tra le schermate
                BackHandler(enabled = screen != "home") {
                    screen = "home"
                }

                // Dialog del tutorial step-by-step
                if (showTutorial) {
                    TutorialDialog(onDismiss = { showTutorial = false })
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Navigazione principale gestita tramite un blocco when (Simple Navigation)
                    when (screen) {
                        "home" -> {
                            // Resetta lo stato del quiz quando l'utente torna alla home
                            LaunchedEffect(Unit) {
                                quizViewModel.resetQuiz()
                            }

                            HomeScreen(
                                sections = defaultQuizSections,
                                selectedSection = selectedSection,
                                onSectionSelected = { selectedSection = it },
                                onModeSelected = { mode ->
                                    selectedMode = mode
                                    when (mode) {
                                        QuizMode.STUDIO -> screen = "study"
                                        QuizMode.SMART -> {
                                            quizViewModel.loadSmartQuiz(selectedSection.tags, 20)
                                            screen = "quiz"
                                        }
                                        QuizMode.ESAME -> screen = "exam_config"
                                    }
                                },
                                onStatsClick = { screen = "stats" },
                                onSettingsClick = { screen = "settings" },
                                onTutorialClick = { showTutorial = true },
                                onAdminClick = { 
                                    if (adminDisclaimerConfirmed) {
                                        screen = "editor"
                                    } else {
                                        showAdminDisclaimer = true
                                    }
                                },
                                isAltered = isDatabaseAltered,
                                onAlteredClick = { showAlteredDisclaimer = true },
                                isLoading = uiState.isLoading
                            )
                        }

                        "exam_config" -> {
                            // Schermata di scelta del numero di domande per l'esame
                            ExamConfigScreen(
                                onStartExam = { count ->
                                    quizViewModel.loadExam(count, selectedSection.tags)
                                    screen = "quiz"
                                },
                                onBack = { screen = "home" }
                            )
                        }

                        "login" -> {
                            // Schermata rimossa - reindirizzamento alla home
                            LaunchedEffect(Unit) { screen = "home" }
                        }

                        "editor" -> {
                            // Interfaccia di gestione del database (CRUD domande)
                            val allQuestions by quizViewModel.databaseQuestionsFlow.collectAsState()
                            QuestionEditorScreen(
                                questions = allQuestions,
                                onSaveQuestion = { quizViewModel.saveQuestion(it) },
                                onDeleteQuestion = { quizViewModel.deleteQuestion(it) },
                                onBack = { screen = "home" }
                            )
                        }

                        "stats" -> {
                            // Visualizzazione delle statistiche aggregate e dell'indice di preparazione
                            val categoryStats = remember(questionStats, databaseQuestions) {
                                quizViewModel.getCategoryStats()
                            }

                            val prepIndex = remember(questionStats, databaseQuestions) {
                                quizViewModel.calculatePreparationIndex()
                            }

                            StatsScreen(
                                categoryStats = categoryStats,
                                prepIndex = prepIndex,
                                onBack = { screen = "home" },
                                onHome = { screen = "home" },
                                onResetStats = { quizViewModel.resetStats() },
                                getQuestionStats = { category, tag ->
                                    quizViewModel.getQuestionStatsByCategory(category, tag)
                                }
                            )
                        }

                        "settings" -> {
                            // Schermata delle preferenze e link ai documenti legali/guide
                            SettingsScreen(
                                state = settingsState,
                                onThemeChanged = { settingsViewModel.setTheme(it) },
                                onFontSizeChanged = { settingsViewModel.setFontSizeMultiplier(it) },
                                onHapticChanged = { settingsViewModel.setHapticEnabled(it) },
                                onExportDatabase = { shareDatabaseFile() },
                                onImportDatabase = { databasePicker.launch("application/json") },
                                onExportGitHub = { shareModifiedQuestionsFile(asGitHubIssue = true) },
                                onShareModifications = { shareModifiedQuestionsFile(asGitHubIssue = false) },
                                onOpenPrivacy = { openPrivacyPolicy() },
                                onViewCode = { openGitHubRepo() },
                                onShowDisclaimer = { screen = "disclaimer" },
                                onShowGuide = { screen = "guide" },
                                onBack = { screen = "home" }
                            )
                        }

                        "disclaimer" -> {
                            // Visualizzazione del disclaimer richiamata dalle impostazioni
                            DisclaimerDialog(
                                onAccept = {},
                                showAcceptButton = false,
                                onDismiss = { screen = "settings" }
                            )
                        }

                        "guide" -> {
                            // Approfondimento sulle logiche di calcolo dell'app
                            AppLogicGuideScreen(
                                onBack = { screen = "settings" }
                            )
                        }

                        "study" -> {
                            // Selezione del capitolo per la modalità Studio
                            val categoryStats = remember(questionStats, databaseQuestions) {
                                quizViewModel.getCategoryStats(selectedSection.tags)
                            }

                            if (showStudyConfig) {
                                StudyConfigDialog(
                                    onConfirm = { count ->
                                        quizViewModel.loadQuiz(
                                            QuizMode.STUDIO,
                                            selectedSection.tags,
                                            count
                                        )
                                        showStudyConfig = false
                                        screen = "quiz"
                                    },
                                    onDismiss = { showStudyConfig = false }
                                )
                            }

                            StudyScreen(
                                categoryStats = categoryStats,
                                sectionLabel = selectedSection.label,
                                onCategorySelected = { category ->
                                    quizViewModel.loadQuizByCategory(
                                        category,
                                        QuizMode.STUDIO,
                                        selectedSection.tags
                                    )
                                    screen = "quiz"
                                },
                                onAllQuestionsSelected = {
                                    showStudyConfig = true
                                }
                            )
                        }

                        "quiz" -> {
                            // La schermata del quiz attivo (Studio, SMART o Esame)
                            QuizScreen(
                                state = uiState,
                                hapticEnabled = settingsState.hapticEnabled,
                                onAnswerClick = { quizViewModel.answer(it) },
                                onConfirmAnswer = { quizViewModel.confirmAnswer() },
                                onNextQuestion = { quizViewModel.nextQuestion() },
                                onSubmitExam = { quizViewModel.submitExam() },
                                onRestart = {
                                    quizViewModel.resetQuiz()
                                    screen = "home"
                                },
                                onSubmitClick = { quizViewModel.checkBeforeSubmit() },
                                onCloseDialog = { quizViewModel.closeSubmitDialog() },
                                onReportQuestionError = { reportQuestionToProcess = it }
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Gestisce la condivisione del file database.json tramite Intent.
     */
    private fun shareDatabaseFile() {
        val file = quizRepository.getDatabaseFile()
        if (!file.exists()) return

        val uri = androidx.core.content.FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(android.content.Intent.createChooser(intent, "Esporta Database"))
    }

    /**
     * Esporta solo le domande nuove o modificate tramite GitHub Issue o Email.
     */
    private fun shareModifiedQuestionsFile(asGitHubIssue: Boolean) {
        lifecycleScope.launch {
            val modified = quizRepository.getModifiedQuestions()
            if (modified.isEmpty()) {
                Toast.makeText(this@MainActivity, "Nessuna modifica da esportare", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val json = com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(modified)
            
            if (asGitHubIssue) {
                val body = "Sottopongo le seguenti modifiche al database delle domande.\n\n" +
                        "```json\n$json\n```"
                
                // Se il JSON è troppo lungo per un URL (limite prudenziale ~1500), usiamo lo sharing file
                if (body.length > 1500) {
                    shareAsFile(modified, "Allego il file con le mie modifiche per la revisione.")
                } else {
                    val url = "https://github.com/Overlos/QuizSoccorso/issues/new?title=Export Modifiche Database&body=${android.net.Uri.encode(body)}"
                    startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                }
            } else {
                // Condivisione universale (Email pre-compilata)
                val body = "Ciao,\n\nin allegato trovi le modifiche apportate al database dei quiz.\n\n" +
                        "Dettaglio modifiche:\n" +
                        "------------------\n" +
                        json.take(500) + (if (json.length > 500) "..." else "") + "\n" +
                        "------------------\n"
                
                shareAsFile(modified, body, "overlos.dev@gmail.com")
            }
        }
    }

    private suspend fun shareAsFile(modified: List<QuizQuestion>, body: String, recipient: String? = null) {
        val file = quizRepository.saveToTempFile(modified, "modified_questions.json") ?: return
        val uri = androidx.core.content.FileProvider.getUriForFile(
            this@MainActivity,
            "${packageName}.fileprovider",
            file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/json"
            if (recipient != null) {
                putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(recipient))
            }
            putExtra(android.content.Intent.EXTRA_SUBJECT, "QuizSoccorso - Proposta Modifica Domande")
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_TEXT, body)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(android.content.Intent.createChooser(intent, "Invia Modifiche"))
    }

    /**
     * Apre la Privacy Policy.
     */
    private fun openPrivacyPolicy() {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://overlos.github.io/QuizSoccorso/"))
        startActivity(intent)
    }

    /**
     * Apre GitHub Issues pre-compilando il body con info sulla domanda o modifiche.
     */
    private fun openGitHubIssues(question: QuizQuestion? = null) {
        val title = if (question != null) "Segnalazione Errore Domanda ID ${question.id}" else "Segnalazione Generica"
        val body = if (question != null) {
            "Errore riscontrato nella seguente domanda:\n\n" +
                    "ID: ${question.id}\n" +
                    "Testo: ${question.question}\n" +
                    "Categoria: ${question.category}\n\n" +
                    "Descrizione errore: "
        } else {
            "Descrivi il problema..."
        }

        val url = "https://github.com/Overlos/QuizSoccorso/issues/new?title=${android.net.Uri.encode(title)}&body=${android.net.Uri.encode(body)}"
        startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
    }

    /**
     * Apre la repository GitHub principale.
     */
    private fun openGitHubRepo() {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/Overlos/QuizSoccorso"))
        startActivity(intent)
    }

    /**
     * Invia una segnalazione via email per una specifica domanda.
     */
    private fun sendErrorEmail(question: QuizQuestion) {
        val subject = "Segnalazione Errore QuizSoccorso - ID ${question.id}"
        val body = "Segnalazione errore per la domanda ID: ${question.id}\n\n" +
                "Testo: ${question.question}\n" +
                "Categoria: ${question.category}\n\n" +
                "Descrizione dell'errore:\n"
        
        val uriString = "mailto:overlos.dev@gmail.com" +
                "?subject=${android.net.Uri.encode(subject)}" +
                "&body=${android.net.Uri.encode(body)}"
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse(uriString)
        }
        
        try {
            startActivity(intent)
        } catch (_: Exception) {
            // Fallback nel caso in cui il parsing dell'URI lungo fallisca su alcuni client
            val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:overlos.dev@gmail.com")
                putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
                putExtra(android.content.Intent.EXTRA_TEXT, body)
            }
            try {
                startActivity(fallbackIntent)
            } catch (_: Exception) {
                Toast.makeText(this, "Nessuna app email trovata", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class QuizViewModelFactory(
    private val repository: QuizRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            return QuizViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
