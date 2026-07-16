package com.example.quizsoccorso

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
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
    private val context: Context
) {

    // Gson con "Pretty Printing" rende i file JSON leggibili anche da un umano se aperti
    private val gson = GsonBuilder().setPrettyPrinting().create()

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
        return parseJson(loadAssetJson())
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
     */
    suspend fun getModifiedQuestions(): List<QuizQuestion> = withContext(Dispatchers.IO) {
        val current = loadQuestions()
        val original = loadAssetQuestions()
        val originalMap = original.associateBy { it.id }

        current.filter { q ->
            val orig = originalMap[q.id]
            // Una domanda è considerata modificata se il suo ID non esisteva o se il contenuto è diverso
            orig == null || orig != q
        }
    }

    /**
     * Salva le domande su disco.
     * Context.MODE_PRIVATE assicura che il file sia accessibile solo a questa applicazione.
     */
    suspend fun saveQuestions(questions: List<QuizQuestion>) = withContext(Dispatchers.IO) {
        val sanitized = ensureUniqueIds(questions)
        val json = gson.toJson(sanitized)
        context.openFileOutput("quiz.json", Context.MODE_PRIVATE).use { 
            it.write(json.toByteArray()) 
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
            if (q.id <= 0 || q.id in usedIds) {
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
     * Salva le statistiche. 
     * NOTA: Questa operazione è onerosa in termini di I/O, va chiamata con parsimonia.
     */
    suspend fun saveStats(stats: Map<Int, QuestionStat>) = withContext(Dispatchers.IO) {
        val json = gson.toJson(stats)
        context.openFileOutput(STATS_FILE, Context.MODE_PRIVATE).use { 
            it.write(json.toByteArray()) 
        }
    }

    companion object {
        private const val STATS_FILE = "quiz_stats.json"
    }
}
