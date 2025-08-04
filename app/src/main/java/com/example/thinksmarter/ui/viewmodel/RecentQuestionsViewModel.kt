package com.example.thinksmarter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinksmarter.data.model.QuestionWithAnswer
import com.example.thinksmarter.domain.repository.ThinkSmarterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecentQuestionsUiState(
    val questionsWithAnswers: List<QuestionWithAnswer> = emptyList(),
    val isLoading: Boolean = true
)

class RecentQuestionsViewModel(
    private val repository: ThinkSmarterRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecentQuestionsUiState())
    val uiState: StateFlow<RecentQuestionsUiState> = _uiState.asStateFlow()
    
    init {
        loadQuestions()
    }
    
    private fun loadQuestions() {
        viewModelScope.launch {
            repository.getAllQuestionsWithAnswers().collect { questionsWithAnswers ->
                _uiState.value = _uiState.value.copy(
                    questionsWithAnswers = questionsWithAnswers,
                    isLoading = false
                )
            }
        }
    }
} 