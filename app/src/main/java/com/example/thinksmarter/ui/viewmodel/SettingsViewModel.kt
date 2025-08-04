package com.example.thinksmarter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinksmarter.domain.repository.ThinkSmarterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val apiKey: String = "",
    val difficultyLevel: Int = 5,
    val selectedCategory: String = "General",
    val lengthPreference: String = "Auto", // Auto, Short, Medium, Long
    val themePreference: String = "Dark", // Dark, Light, Auto
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

sealed class SettingsUiEvent {
    data class UpdateApiKey(val apiKey: String) : SettingsUiEvent()
    data class UpdateDifficultyLevel(val level: Int) : SettingsUiEvent()
    data class UpdateSelectedCategory(val category: String) : SettingsUiEvent()
    data class UpdateLengthPreference(val preference: String) : SettingsUiEvent()
    data class UpdateThemePreference(val theme: String) : SettingsUiEvent()
    object SaveSettings : SettingsUiEvent()
    object ClearApiKey : SettingsUiEvent()
    object ClearError : SettingsUiEvent()
    object ClearSuccessMessage : SettingsUiEvent()
}

class SettingsViewModel(
    private val repository: ThinkSmarterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun handleEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.UpdateApiKey -> updateApiKey(event.apiKey)
            is SettingsUiEvent.UpdateDifficultyLevel -> updateDifficultyLevel(event.level)
            is SettingsUiEvent.UpdateSelectedCategory -> updateSelectedCategory(event.category)
            is SettingsUiEvent.UpdateLengthPreference -> updateLengthPreference(event.preference)
            is SettingsUiEvent.UpdateThemePreference -> updateThemePreference(event.theme)
            is SettingsUiEvent.SaveSettings -> saveSettings()
            is SettingsUiEvent.ClearApiKey -> clearApiKey()
            is SettingsUiEvent.ClearError -> clearError()
            is SettingsUiEvent.ClearSuccessMessage -> clearSuccessMessage()
        }
    }

    private fun updateApiKey(apiKey: String) {
        _uiState.value = _uiState.value.copy(apiKey = apiKey)
    }

    private fun updateDifficultyLevel(level: Int) {
        _uiState.value = _uiState.value.copy(difficultyLevel = level)
    }

    private fun updateSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    private fun updateLengthPreference(preference: String) {
        _uiState.value = _uiState.value.copy(lengthPreference = preference)
    }

    private fun updateThemePreference(theme: String) {
        _uiState.value = _uiState.value.copy(themePreference = theme)
    }

    private fun saveSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                successMessage = null
            )
            
            try {
                val apiKey = _uiState.value.apiKey.trim()
                if (apiKey.isEmpty()) { throw Exception("API key cannot be empty") }
                if (apiKey.length < 20) { throw Exception("API key appears to be too short. Please check your key.") }
                if (apiKey.contains("🧱") || apiKey.contains("Functional Requirements") || apiKey.contains("MVP Scope")) {
                    throw Exception("Invalid API key detected. Please enter your actual Anthropic API key, not the requirements text.")
                }
                repository.setApiKey(apiKey)
                repository.setDifficultyLevel(_uiState.value.difficultyLevel)
                repository.setSelectedCategory(_uiState.value.selectedCategory)
                repository.setLengthPreference(_uiState.value.lengthPreference)
                repository.setThemePreference(_uiState.value.themePreference)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Settings saved successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save settings"
                )
            }
        }
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    private fun clearApiKey() {
        _uiState.value = _uiState.value.copy(apiKey = "")
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val apiKey = repository.getApiKey() ?: ""
                val difficultyLevel = repository.getDifficultyLevel()
                val selectedCategory = repository.getSelectedCategory()
                val lengthPreference = repository.getLengthPreference()
                val themePreference = repository.getThemePreference()
                
                _uiState.value = _uiState.value.copy(
                    apiKey = apiKey,
                    difficultyLevel = difficultyLevel,
                    selectedCategory = selectedCategory,
                    lengthPreference = lengthPreference,
                    themePreference = themePreference
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load settings"
                )
            }
        }
    }
} 