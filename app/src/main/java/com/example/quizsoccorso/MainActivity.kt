package com.example.quizsoccorso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.example.quizsoccorso.ui.theme.QuizSoccorsoTheme

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

                // Osservazione dello stato principale della UI del quiz e delle statistiche
                val uiState by quizViewModel.uiState.collectAsState()
                val questionStats by quizViewModel.questionStats.collectAsState()

                // Visualizzazione del disclaimer legale se non è ancora stato accettato (primo avvio)
                if (!settingsState.disclaimerAccepted) {
                    DisclaimerDialog(
                        onAccept = { settingsViewModel.acceptDisclaimer() }
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
                                onImportClick = { databasePicker.launch("application/json") },
                                onStatsClick = { screen = "stats" },
                                onSettingsClick = { screen = "settings" },
                                onTutorialClick = { showTutorial = true },
                                onAdminClick = { screen = "login" },
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
                            // Accesso protetto all'area editor
                            AdminLoginScreen(
                                onLoginSuccess = { screen = "editor" },
                                onBack = { screen = "home" }
                            )
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
                                onCloseDialog = { quizViewModel.closeSubmitDialog() }
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
