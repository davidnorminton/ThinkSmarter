package com.example.thinksmarter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinksmarter.data.model.TextImprovement
import com.example.thinksmarter.domain.repository.ThinkSmarterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TextImprovementUiState(
    val userText: String = "",
    val selectedTextType: String = "General",
    val isAnalyzing: Boolean = false,
    val evaluation: TextImprovement? = null,
    val error: String? = null,
    val textImprovements: List<TextImprovement> = emptyList(),
    val isLoading: Boolean = true
)

sealed class TextImprovementUiEvent {
    object AnalyzeText : TextImprovementUiEvent()
    object ClearError : TextImprovementUiEvent()
    data class UpdateUserText(val text: String) : TextImprovementUiEvent()
    data class UpdateTextType(val textType: String) : TextImprovementUiEvent()
    object ClearEvaluation : TextImprovementUiEvent()
}

@HiltViewModel
class TextImprovementViewModel @Inject constructor(
    private val repository: ThinkSmarterRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TextImprovementUiState())
    val uiState: StateFlow<TextImprovementUiState> = _uiState.asStateFlow()
    
    init {
        println("DEBUG: Initializing TextImprovementViewModel")
        try {
            loadTextImprovements()
        } catch (e: Exception) {
            println("DEBUG: Error in TextImprovementViewModel init: ${e.message}")
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(
                error = "Failed to load text improvements: ${e.message}",
                isLoading = false
            )
        }
    }
    
    fun onEvent(event: TextImprovementUiEvent) {
        when (event) {
            is TextImprovementUiEvent.AnalyzeText -> analyzeText()
            is TextImprovementUiEvent.ClearError -> clearError()
            is TextImprovementUiEvent.UpdateUserText -> updateUserText(event.text)
            is TextImprovementUiEvent.UpdateTextType -> updateTextType(event.textType)
            is TextImprovementUiEvent.ClearEvaluation -> clearEvaluation()
        }
    }
    
    private fun analyzeText() {
        viewModelScope.launch {
            val userText = _uiState.value.userText.trim()
            
            if (userText.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    error = "Please enter some text to analyze"
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(
                isAnalyzing = true,
                error = null
            )
            
            try {
                val evaluation = repository.improveText(userText, _uiState.value.selectedTextType)
                evaluation.fold(
                    onSuccess = { result ->
                        val textImprovement = TextImprovement(
                            originalText = userText,
                            textType = _uiState.value.selectedTextType,
                            clarityScore = result.clarityScore,
                            logicScore = result.logicScore,
                            perspectiveScore = result.perspectiveScore,
                            depthScore = result.depthScore,
                            feedback = result.feedback,
                            wordAndPhraseSuggestions = result.wordAndPhraseSuggestions,
                            betterAnswerSuggestions = result.betterAnswerSuggestions,
                            thoughtProcessGuidance = result.thoughtProcessGuidance,
                            modelAnswer = result.modelAnswer
                        )
                        
                        repository.insertTextImprovement(textImprovement)
                        
                        _uiState.value = _uiState.value.copy(
                            evaluation = textImprovement,
                            isAnalyzing = false
                        )
                        
                        loadTextImprovements()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isAnalyzing = false,
                            error = exception.message ?: "Failed to analyze text"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    error = e.message ?: "Failed to analyze text"
                )
            }
        }
    }
    
    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun updateUserText(text: String) {
        _uiState.value = _uiState.value.copy(userText = text)
    }
    
    private fun updateTextType(textType: String) {
        _uiState.value = _uiState.value.copy(selectedTextType = textType)
    }
    
    private fun clearEvaluation() {
        _uiState.value = _uiState.value.copy(evaluation = null)
    }
    
    private fun loadTextImprovements() {
        viewModelScope.launch {
            repository.getAllTextImprovements().collect { textImprovements ->
                _uiState.value = _uiState.value.copy(
                    textImprovements = textImprovements,
                    isLoading = false
                )
            }
        }
    }
}
