package com.example.thinksmarter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinksmarter.domain.repository.ThinkSmarterRepository
import com.example.thinksmarter.data.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RandomFactsUiState(
    val currentFact: String? = null,
    val selectedCategory: String = "General",
    val availableCategories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class RandomFactsUiEvent {
    object GenerateRandomFact : RandomFactsUiEvent()
    data class SelectCategory(val category: String) : RandomFactsUiEvent()
    object ClearError : RandomFactsUiEvent()
}

class RandomFactsViewModel(
    private val repository: ThinkSmarterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RandomFactsUiState())
    val uiState: StateFlow<RandomFactsUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun handleEvent(event: RandomFactsUiEvent) {
        when (event) {
            is RandomFactsUiEvent.GenerateRandomFact -> generateRandomFact()
            is RandomFactsUiEvent.SelectCategory -> selectCategory(event.category)
            is RandomFactsUiEvent.ClearError -> clearError()
        }
    }

    private fun generateRandomFact() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val result = repository.generateRandomFact(_uiState.value.selectedCategory)
                result.fold(
                    onSuccess = { fact ->
                        _uiState.value = _uiState.value.copy(
                            currentFact = fact,
                            isLoading = false
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to generate random fact: ${exception.message}",
                            isLoading = false
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error generating random fact: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            currentFact = null // Clear current fact when category changes
        )
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                repository.getAllCategories().collect { categories ->
                    _uiState.value = _uiState.value.copy(availableCategories = categories)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load categories: ${e.message}"
                )
            }
        }
    }
} 