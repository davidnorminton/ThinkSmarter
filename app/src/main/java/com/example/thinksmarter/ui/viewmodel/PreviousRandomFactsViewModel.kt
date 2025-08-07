package com.example.thinksmarter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinksmarter.data.model.RandomFact
import com.example.thinksmarter.domain.repository.ThinkSmarterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PreviousRandomFactsUiState(
    val facts: List<RandomFact> = emptyList(),
    val selectedCategory: String = "All",
    val availableCategories: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class PreviousRandomFactsUiEvent {
    data class SelectCategory(val category: String) : PreviousRandomFactsUiEvent()
    object LoadFacts : PreviousRandomFactsUiEvent()
    object ClearError : PreviousRandomFactsUiEvent()
}

@HiltViewModel
class PreviousRandomFactsViewModel @Inject constructor(
    private val repository: ThinkSmarterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreviousRandomFactsUiState())
    val uiState: StateFlow<PreviousRandomFactsUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadFacts()
    }

    fun handleEvent(event: PreviousRandomFactsUiEvent) {
        when (event) {
            is PreviousRandomFactsUiEvent.SelectCategory -> selectCategory(event.category)
            is PreviousRandomFactsUiEvent.LoadFacts -> loadFacts()
            is PreviousRandomFactsUiEvent.ClearError -> clearError()
        }
    }

    private fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadFacts()
    }

    private fun loadFacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val facts = if (_uiState.value.selectedCategory == "All") {
                    repository.getAllRandomFacts().first()
                } else {
                    repository.getRandomFactsByCategory(_uiState.value.selectedCategory).first()
                }
                
                _uiState.value = _uiState.value.copy(
                    facts = facts,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load facts: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                repository.getAllCategories().collect { categories ->
                    val categoryNames = categories.map { it.name }
                    _uiState.value = _uiState.value.copy(availableCategories = categoryNames)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load categories: ${e.message}"
                )
            }
        }
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 