package com.example.thinksmarter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinksmarter.data.model.Question
import com.example.thinksmarter.data.model.Answer
import com.example.thinksmarter.data.model.QuestionWithAnswer
import com.example.thinksmarter.domain.repository.ThinkSmarterRepository
import com.example.thinksmarter.domain.repository.AnswerEvaluation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.thinksmarter.data.model.Category

data class MainUiState(
    val currentQuestion: Question? = null,
    val userAnswer: String = "",
    val evaluation: AnswerEvaluation? = null,
    val questionsWithAnswers: List<QuestionWithAnswer> = emptyList(),
    val isGeneratingQuestion: Boolean = false,
    val isEvaluatingAnswer: Boolean = false,
    val error: String? = null,
    val difficultyLevel: Int = 5,
    val selectedCategory: String = "General",
    val lengthPreference: String = "Auto",
    val availableCategories: List<Category> = emptyList(),
    val isLoading: Boolean = true // Added for splash screen
)

sealed class MainUiEvent {
    object GenerateQuestion : MainUiEvent()
    data class UpdateUserAnswer(val answer: String) : MainUiEvent()
    object SubmitAnswer : MainUiEvent()
    object ClearError : MainUiEvent()
    data class SetDifficultyLevel(val level: Int) : MainUiEvent()
    data class SetSelectedCategory(val category: String) : MainUiEvent()
}

class MainViewModel(
    private val repository: ThinkSmarterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
        loadDifficultyLevel()
        loadSelectedCategory()
        loadLengthPreference()
        loadCategories()
        
        // Set loading to false after initialization
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000) // Show splash for 2 seconds
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun handleEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.GenerateQuestion -> generateQuestion()
            is MainUiEvent.UpdateUserAnswer -> updateUserAnswer(event.answer)
            is MainUiEvent.SubmitAnswer -> submitAnswer()
            is MainUiEvent.ClearError -> clearError()
            is MainUiEvent.SetDifficultyLevel -> setDifficultyLevel(event.level)
            is MainUiEvent.SetSelectedCategory -> setSelectedCategory(event.category)
        }
    }

    private fun generateQuestion() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGeneratingQuestion = true,
                error = null,
                evaluation = null, // Clear previous evaluation
                userAnswer = "" // Clear previous answer
            )
            
            val questionText = repository.generateQuestion(
                _uiState.value.difficultyLevel,
                _uiState.value.selectedCategory,
                _uiState.value.lengthPreference
            )
            
            questionText.fold(
                onSuccess = { text ->
                    val expectedLength = when (_uiState.value.lengthPreference) {
                        "Short" -> "Short"
                        "Medium" -> "Medium"
                        "Long" -> "Long"
                        else -> when {
                            _uiState.value.difficultyLevel <= 3 -> "Short"
                            _uiState.value.difficultyLevel <= 7 -> "Medium"
                            else -> "Long"
                        }
                    }
                    
                    val question = Question(
                        text = text,
                        difficulty = _uiState.value.difficultyLevel,
                        category = _uiState.value.selectedCategory,
                        expectedAnswerLength = expectedLength
                    )
                    
                    val questionId = repository.insertQuestion(question)
                    val savedQuestion = question.copy(id = questionId)
                    
                    _uiState.value = _uiState.value.copy(
                        currentQuestion = savedQuestion,
                        isGeneratingQuestion = false
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to generate question",
                        isGeneratingQuestion = false
                    )
                }
            )
        }
    }

    private fun updateUserAnswer(answer: String) {
        _uiState.value = _uiState.value.copy(userAnswer = answer)
    }

    private fun submitAnswer() {
        viewModelScope.launch {
            val currentQuestion = _uiState.value.currentQuestion ?: return@launch
            val userAnswer = _uiState.value.userAnswer.trim()
            
            if (userAnswer.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    error = "Please enter your answer"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isEvaluatingAnswer = true,
                error = null
            )

            try {
                val evaluation = repository.evaluateAnswer(
                    currentQuestion.text,
                    userAnswer,
                    currentQuestion.expectedAnswerLength
                )
                evaluation.fold(
                    onSuccess = { result ->
                        val answer = Answer(
                            questionId = currentQuestion.id,
                            userAnswer = userAnswer,
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
                        repository.insertAnswer(answer)
                        
                        _uiState.value = _uiState.value.copy(
                            evaluation = result,
                            isEvaluatingAnswer = false
                        )
                        loadQuestions()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isEvaluatingAnswer = false,
                            error = exception.message ?: "Failed to evaluate answer"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isEvaluatingAnswer = false,
                    error = e.message ?: "Failed to evaluate answer"
                )
            }
        }
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun setDifficultyLevel(level: Int) {
        viewModelScope.launch {
            repository.setDifficultyLevel(level)
            _uiState.value = _uiState.value.copy(difficultyLevel = level)
        }
    }

    private fun setSelectedCategory(category: String) {
        viewModelScope.launch {
            repository.setSelectedCategory(category)
            _uiState.value = _uiState.value.copy(selectedCategory = category)
        }
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            repository.getAllQuestionsWithAnswers().collect { questionsWithAnswers ->
                _uiState.value = _uiState.value.copy(questionsWithAnswers = questionsWithAnswers)
            }
        }
    }

    private fun loadDifficultyLevel() {
        viewModelScope.launch {
            val level = repository.getDifficultyLevel()
            _uiState.value = _uiState.value.copy(difficultyLevel = level)
        }
    }

    private fun loadSelectedCategory() {
        viewModelScope.launch {
            val category = repository.getSelectedCategory()
            _uiState.value = _uiState.value.copy(selectedCategory = category)
        }
    }

    private fun loadLengthPreference() {
        viewModelScope.launch {
            val length = repository.getLengthPreference()
            _uiState.value = _uiState.value.copy(lengthPreference = length)
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(availableCategories = categories)
            }
        }
    }
} 