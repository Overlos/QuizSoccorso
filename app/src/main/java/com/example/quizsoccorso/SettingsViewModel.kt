package com.example.quizsoccorso

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Stato della UI per le impostazioni.
 */
data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val hapticEnabled: Boolean = true,
    val disclaimerAccepted: Boolean = false,
    val fontSizeMultiplier: Float = 1.0f
)

/**
 * ViewModel che gestisce le impostazioni dell'app e la loro persistenza.
 */
class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            theme = repository.getTheme(),
            hapticEnabled = repository.isHapticEnabled(),
            disclaimerAccepted = repository.isDisclaimerAccepted(),
            fontSizeMultiplier = repository.getFontSizeMultiplier()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /**
     * Cambia il tema dell'applicazione.
     */
    fun setTheme(theme: AppTheme) {
        repository.setTheme(theme)
        _uiState.update { it.copy(theme = theme) }
    }

    /**
     * Cambia la dimensione del font.
     */
    fun setFontSizeMultiplier(multiplier: Float) {
        repository.setFontSizeMultiplier(multiplier)
        _uiState.update { it.copy(fontSizeMultiplier = multiplier) }
    }

    /**
     * Abilita o disabilita la vibrazione.
     */
    fun setHapticEnabled(enabled: Boolean) {
        repository.setHapticEnabled(enabled)
        _uiState.update { it.copy(hapticEnabled = enabled) }
    }

    /**
     * Segna il disclaimer come accettato.
     */
    fun acceptDisclaimer() {
        repository.setDisclaimerAccepted(true)
        _uiState.update { it.copy(disclaimerAccepted = true) }
    }
}
