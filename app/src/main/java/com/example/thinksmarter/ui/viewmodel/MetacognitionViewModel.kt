package com.example.thinksmarter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinksmarter.domain.repository.ThinkSmarterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MetacognitionUiState(
    val userInput: String = "",
    val guidance: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class MetacognitionUiEvent {
    data class UpdateInput(val input: String) : MetacognitionUiEvent()
    object Submit : MetacognitionUiEvent()
    object ClearError : MetacognitionUiEvent()
}

@HiltViewModel
class MetacognitionViewModel @Inject constructor(
    private val repository: ThinkSmarterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetacognitionUiState())
    val uiState: StateFlow<MetacognitionUiState> = _uiState.asStateFlow()

    fun handleEvent(event: MetacognitionUiEvent) {
        when (event) {
            is MetacognitionUiEvent.UpdateInput -> updateInput(event.input)
            is MetacognitionUiEvent.Submit -> generateMetacognitiveGuidance()
            is MetacognitionUiEvent.ClearError -> clearError()
        }
    }

    private fun updateInput(input: String) {
        _uiState.value = _uiState.value.copy(userInput = input)
    }

    private fun generateMetacognitiveGuidance() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val result = repository.generateMetacognitiveGuidance(_uiState.value.userInput)
                result.fold(
                    onSuccess = { guidance ->
                        _uiState.value = _uiState.value.copy(
                            guidance = guidance,
                            isLoading = false
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to generate guidance: ${exception.message}",
                            isLoading = false
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error generating guidance: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 