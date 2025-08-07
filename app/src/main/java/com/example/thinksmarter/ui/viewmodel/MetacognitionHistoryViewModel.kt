package com.example.thinksmarter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinksmarter.data.model.MetacognitionResponse
import com.example.thinksmarter.domain.repository.ThinkSmarterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MetacognitionHistoryUiState(
    val responses: List<MetacognitionResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class MetacognitionHistoryUiEvent {
    object LoadResponses : MetacognitionHistoryUiEvent()
    object ClearError : MetacognitionHistoryUiEvent()
}

@HiltViewModel
class MetacognitionHistoryViewModel @Inject constructor(
    private val repository: ThinkSmarterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetacognitionHistoryUiState())
    val uiState: StateFlow<MetacognitionHistoryUiState> = _uiState.asStateFlow()

    init {
        loadResponses()
    }

    fun handleEvent(event: MetacognitionHistoryUiEvent) {
        when (event) {
            is MetacognitionHistoryUiEvent.LoadResponses -> loadResponses()
            is MetacognitionHistoryUiEvent.ClearError -> clearError()
        }
    }

    private fun loadResponses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val responses = repository.getAllMetacognitionResponses().first()
                _uiState.value = _uiState.value.copy(
                    responses = responses,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load responses: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 