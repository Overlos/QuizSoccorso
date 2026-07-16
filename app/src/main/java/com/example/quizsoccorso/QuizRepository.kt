package com.example.quizsoccorso

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository per la gestione dei dati del quiz (domande e statistiche).
 * Gestisce la persistenza su file system locale e l'accesso agli assets integrati nell'APK.
 * Utilizza la libreria Gson per la serializzazione/deserializzazione JSON.
 */
class QuizRepository(
    private val context: Context
) {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * Carica le domande dal database locale (cartella files dell'app) o, 
     * se non presente, dal database predefinito negli assets.
     */
    suspend fun loadQuestions(): List<QuizQuestion> = withContext(Dispatchers.IO) {
        val json = try {
            // Tenta di caricare il database personalizzato caricato dall'utente o editato
            context.openFileInput("quiz.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            // Se non esiste ancora un database locale, usa quello predefinito incluso negli assets
            context.assets
                .open("android_questions.json")
                .bufferedReader()
                .use { it.readText() }
        }
        parseJson(json)
    }

    private fun parseJson(json: String): List<QuizQuestion> {
        val type = object : TypeToken<List<QuizQuestion>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Salva l'intera lista di domande nel database locale (quiz.json).
     * Viene chiamata dopo un'aggiunta, modifica o eliminazione dall'editor.
     */
    suspend fun saveQuestions(questions: List<QuizQuestion>) = withContext(Dispatchers.IO) {
        val json = gson.toJson(questions)
        context.openFileOutput("quiz.json", Context.MODE_PRIVATE)
            .use { it.write(json.toByteArray()) }
    }

    /**
     * Importa un database JSON selezionato tramite il file picker del sistema.
     */
    suspend fun importDatabase(uri: Uri) = withContext(Dispatchers.IO) {
        val json = context.contentResolver
            .openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: return@withContext

        context.openFileOutput("quiz.json", Context.MODE_PRIVATE)
            .use { it.write(json.toByteArray()) }
    }

    /**
     * Fornisce il riferimento al file fisico del database per permetterne la condivisione.
     */
    fun getDatabaseFile(): java.io.File {
        return context.getFileStreamPath("quiz.json")
    }

    /**
     * Carica le statistiche storiche delle risposte date (tentativi, corrette, streak).
     */
    suspend fun loadStats(): Map<Int, QuestionStat> = withContext(Dispatchers.IO) {
        try {
            val json = context.openFileInput(STATS_FILE)
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<Map<Int, QuestionStat>>() {}.type
            gson.fromJson<Map<Int, QuestionStat>>(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Salva le statistiche aggiornate per garantire la persistenza tra le sessioni.
     */
    suspend fun saveStats(stats: Map<Int, QuestionStat>) = withContext(Dispatchers.IO) {
        context.openFileOutput(STATS_FILE, Context.MODE_PRIVATE)
            .use { it.write(gson.toJson(stats).toByteArray()) }
    }

    companion object {
        private const val STATS_FILE = "quiz_stats.json"
    }
}
