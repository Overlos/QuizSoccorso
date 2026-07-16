package com.example.quizsoccorso

import android.content.Context
import android.content.SharedPreferences

enum class AppTheme {
    LIGHT, DARK, SYSTEM, LIGHT_LIVELY, DARK_LIVELY, ACCESSIBLE, OLED, READING
}

/**
 * Repository per la gestione delle preferenze dell'utente e delle impostazioni dell'app.
 * Utilizza SharedPreferences per la persistenza dei dati.
 */
class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /**
     * Recupera il tema dell'app salvato. Default: SYSTEM.
     */
    fun getTheme(): AppTheme {
        val themeStr = prefs.getString("app_theme", AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        return try {
            AppTheme.valueOf(themeStr)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }
    }

    /**
     * Salva la scelta del tema dell'app.
     */
    fun setTheme(theme: AppTheme) {
        prefs.edit().putString("app_theme", theme.name).apply()
    }

    /**
     * Recupera il moltiplicatore della dimensione del font. Default: 1.0f.
     */
    fun getFontSizeMultiplier(): Float {
        return prefs.getFloat("font_size_multiplier", 1.0f)
    }

    /**
     * Salva il moltiplicatore della dimensione del font.
     */
    fun setFontSizeMultiplier(multiplier: Float) {
        prefs.edit().putFloat("font_size_multiplier", multiplier).apply()
    }

    /**
     * Verifica se il feedback aptico (vibrazione) è abilitato. Default: true.
     */
    fun isHapticEnabled(): Boolean {
        return prefs.getBoolean("haptic_enabled", true)
    }

    /**
     * Abilita o disabilita il feedback aptico.
     */
    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_enabled", enabled).apply()
    }

    /**
     * Verifica se il disclaimer legale è già stato accettato. Default: false.
     */
    fun isDisclaimerAccepted(): Boolean {
        return prefs.getBoolean("disclaimer_accepted", false)
    }

    /**
     * Salva lo stato di accettazione del disclaimer.
     */
    fun setDisclaimerAccepted(accepted: Boolean) {
        prefs.edit().putBoolean("disclaimer_accepted", accepted).apply()
    }
}
