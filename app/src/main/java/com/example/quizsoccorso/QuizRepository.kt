package com.example.quizsoccorso

import android.content.Context
import android.net.Uri
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repository per la gestione dei dati del quiz (domande e statistiche).
 *
 * SCOPO DIDATTICO:
 * Il Repository è il punto unico di accesso ai dati (Single Source of Truth).
 * Separa la logica di visualizzazione (UI) dalla persistenza (Salvataggio su file).
 * In questo progetto usiamo il file system interno dell'app e file in formato JSON.
 */
class QuizRepository(
    private val context: Context,
) {

    // Gson con "Pretty Printing" rende i file JSON leggibili anche da un umano se aperti
    private val gson = GsonBuilder().setPrettyPrinting().create()

    // Cache in memoria delle domande originali per velocizzare i confronti
    private var originalQuestionsCache: List<QuizQuestion>? = null

    /**
     * Carica le domande dal database locale o dagli assets.
     * Dispatchers.IO sposta l'operazione su un thread secondario dedicato all'Input/Output,
     * evitando di "congelare" l'interfaccia grafica durante la lettura del file.
     */
    suspend fun loadQuestions(): List<QuizQuestion> = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "quiz.json")
        val json = if (file.exists()) {
            file.readText()
        } else {
            // Se l'utente non ha mai modificato nulla, partiamo dai dati originali nell'APK
            loadAssetJson()
        }
        parseJson(json)
    }

    private fun loadAssetJson(): String {
        return context.assets
            .open("android_questions.json")
            .bufferedReader()
            .use { it.readText() }
    }

    private fun loadAssetQuestions(): List<QuizQuestion> {
        return originalQuestionsCache ?: parseJson(loadAssetJson()).also { 
            originalQuestionsCache = it 
        }
    }

    /**
     * Trasforma una stringa JSON in una lista di oggetti Kotlin [QuizQuestion].
     * TypeToken è necessario perché Gson deve sapere che si tratta di una List generica a runtime.
     */
    private fun parseJson(json: String): List<QuizQuestion> {
        return try {
            val type = object : TypeToken<List<QuizQuestion>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Identifica le domande aggiunte o modificate. Utile per l'icona di avviso in Home.
     * Il confronto viene fatto sul contenuto per evitare falsi positivi dovuti al cambio di ID.
     */
    suspend fun getModifiedQuestions(): List<QuizQuestion> = withContext(Dispatchers.IO) {
        val current = loadQuestions()
        val original = loadAssetQuestions()
        
        // Creiamo una mappa basata sul contenuto per una ricerca efficiente
        val originalContentSet = original.asSequence().map { it.toContentString() }.toSet()

        current.filter { it.toContentString() !in originalContentSet }
    }

    private fun QuizQuestion.toContentString(): String {
        return "${question.trim()}|${category.trim()}|${correct.trim()}|${answers.sorted().joinToString(",")}|${tags.sorted().joinToString(",")}"
    }

    /**
     * Salva le domande su disco in modo atomico.
     */
    suspend fun saveQuestions(questions: List<QuizQuestion>) = withContext(Dispatchers.IO) {
        val sanitized = ensureUniqueIds(questions)
        val json = gson.toJson(sanitized)
        
        // Prima di salvare, creiamo un backup del file attuale
        createBackup(fileName = "quiz.json")
        
        writeAtomic("quiz.json", json)
    }

    /**
     * Esegue una scrittura atomica: scrive su un file temporaneo e poi lo rinomina.
     * Questo previene la corruzione dei dati in caso di crash durante la scrittura.
     */
    private fun writeAtomic(fileName: String, content: String) {
        val file = File(context.filesDir, fileName)
        val tmpFile = File(context.filesDir, "$fileName.tmp")
        
        try {
            tmpFile.writeText(content)
            if (!tmpFile.renameTo(file)) {
                // Se il rename fallisce (raro), usiamo il metodo classico come fallback
                file.writeText(content)
            }
        } catch (e: Exception) {
            tmpFile.delete()
            throw e
        }
    }

    /**
     * Crea una copia di backup del file specificato.
     */
    private fun createBackup(fileName: String) {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            val backupFile = File(context.filesDir, "$fileName.bak")
            file.copyTo(backupFile, overwrite = true)
        }
    }

    /**
     * Algoritmo di sanitizzazione degli ID:
     * Assicura che non ci siano ID duplicati o ID a zero, che romperebbero la logica delle statistiche.
     */
    private fun ensureUniqueIds(questions: List<QuizQuestion>): List<QuizQuestion> {
        val usedIds = mutableSetOf<Int>()
        var nextId = (questions.maxOfOrNull { it.id } ?: 0) + 1
        
        return questions.map { q ->
            if (q.id <= 0 || (q.id in usedIds)) {
                val newQ = q.copy(id = nextId++)
                usedIds.add(newQ.id)
                newQ
            } else {
                usedIds.add(q.id)
                q
            }
        }
    }

    /**
     * Importa un database esterno.
     *DistinctBy evita di importare domande identiche a quelle già presenti (stesso testo e risposte).
     */
    suspend fun importDatabase(uri: Uri) = withContext(Dispatchers.IO) {
        val json = context.contentResolver
            .openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: return@withContext

        val imported = parseJson(json)
        val current = loadQuestions()
        
        val combined = (current + imported).distinctBy { 
            it.question.trim().lowercase() + it.answers.sorted().joinToString() 
        }
        
        saveQuestions(combined)
    }

    suspend fun saveToTempFile(questions: List<QuizQuestion>, fileName: String): File? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, fileName)
            file.writeText(gson.toJson(questions))
            file
        } catch (e: Exception) {
            null
        }
    }

    fun getDatabaseFile(): File {
        return File(context.filesDir, "quiz.json")
    }

    /**
     * Carica le statistiche (ID_DOMANDA -> DATI_PROGRESSO).
     */
    suspend fun loadStats(): Map<Int, QuestionStat> = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, STATS_FILE)
        if (!file.exists()) return@withContext emptyMap()

        try {
            val json = file.readText()
            val type = object : TypeToken<Map<Int, QuestionStat>>() {}.type
            gson.fromJson<Map<Int, QuestionStat>>(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Salva le statistiche in modo atomico.
     */
    suspend fun saveStats(stats: Map<Int, QuestionStat>) = withContext(Dispatchers.IO) {
        val json = gson.toJson(stats)
        writeAtomic(STATS_FILE, json)
    }

    companion object {
        private const val STATS_FILE = "quiz_stats.json"
    }
}
