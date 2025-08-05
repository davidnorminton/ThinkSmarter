package com.example.thinksmarter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinksmarter.data.model.Question
import com.example.thinksmarter.data.model.Answer
import com.example.thinksmarter.data.model.QuestionWithAnswer
import com.example.thinksmarter.domain.repository.ThinkSmarterRepository
import com.example.thinksmarter.domain.repository.AnswerEvaluation
import com.example.thinksmarter.data.auth.UserProfile
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
    val isLoading: Boolean = true, // Added for splash screen
    val userProfile: UserProfile? = null // Added for user profile
)

sealed class MainUiEvent {
    object GenerateQuestion : MainUiEvent()
    data class UpdateUserAnswer(val answer: String) : MainUiEvent()
    object SubmitAnswer : MainUiEvent()
    object ClearError : MainUiEvent()
    data class SetDifficultyLevel(val level: Int) : MainUiEvent()
    data class SetSelectedCategory(val category: String) : MainUiEvent()
    data class UpdateUserProfile(val userProfile: UserProfile?) : MainUiEvent()
}

class MainViewModel(
    private val repository: ThinkSmarterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        println("DEBUG: Initializing MainViewModel")
        try {
            loadQuestions()
            loadDifficultyLevel()
            loadSelectedCategory()
            loadLengthPreference()
            loadCategories()
            
            // Set loading to false after initialization
            viewModelScope.launch {
                println("DEBUG: Starting splash screen delay")
                kotlinx.coroutines.delay(2000) // Show splash for 2 seconds
                _uiState.value = _uiState.value.copy(isLoading = false)
                println("DEBUG: Splash screen delay completed")
            }
            println("DEBUG: MainViewModel initialization completed")
        } catch (e: Exception) {
            println("DEBUG: Error in MainViewModel initialization: ${e.message}")
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(
                error = "Failed to initialize: ${e.message}",
                isLoading = false
            )
        }
    }

    fun handleEvent(event: MainUiEvent) {
        println("DEBUG: Handling event: $event")
        when (event) {
            is MainUiEvent.GenerateQuestion -> generateQuestion()
            is MainUiEvent.UpdateUserAnswer -> updateUserAnswer(event.answer)
            is MainUiEvent.SubmitAnswer -> submitAnswer()
            is MainUiEvent.ClearError -> clearError()
            is MainUiEvent.SetDifficultyLevel -> setDifficultyLevel(event.level)
            is MainUiEvent.SetSelectedCategory -> setSelectedCategory(event.category)
            is MainUiEvent.UpdateUserProfile -> updateUserProfile(event.userProfile)
        }
    }

    private fun generateQuestion() {
        println("DEBUG: Generating question")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGeneratingQuestion = true,
                error = null,
                evaluation = null, // Clear previous evaluation
                userAnswer = "" // Clear previous answer
            )
            println("DEBUG: State updated for question generation")
            
            try {
                val questionText = repository.generateQuestion(
                    _uiState.value.difficultyLevel,
                    _uiState.value.selectedCategory,
                    _uiState.value.lengthPreference
                )
                println("DEBUG: Question text generated")
                
                questionText.fold(
                    onSuccess = { text ->
                        println("DEBUG: Processing generated question text")
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
                        
                        println("DEBUG: Inserting question into database")
                        val questionId = repository.insertQuestion(question)
                        val savedQuestion = question.copy(id = questionId)
                        println("DEBUG: Question saved with ID: $questionId")
                        
                        _uiState.value = _uiState.value.copy(
                            currentQuestion = savedQuestion,
                            isGeneratingQuestion = false
                        )
                        println("DEBUG: Question generation completed successfully")
                    },
                    onFailure = { exception ->
                        println("DEBUG: Error generating question: ${exception.message}")
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "Failed to generate question",
                            isGeneratingQuestion = false
                        )
                    }
                )
            } catch (e: Exception) {
                println("DEBUG: Exception in generateQuestion: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to generate question",
                    isGeneratingQuestion = false
                )
            }
        }
    }

    private fun updateUserAnswer(answer: String) {
        _uiState.value = _uiState.value.copy(userAnswer = answer)
    }

    private fun submitAnswer() {
        println("DEBUG: Submitting answer")
        viewModelScope.launch {
            val currentQuestion = _uiState.value.currentQuestion
            if (currentQuestion == null) {
                println("DEBUG: No current question found")
                return@launch
            }
            
            val userAnswer = _uiState.value.userAnswer.trim()
            if (userAnswer.isEmpty()) {
                println("DEBUG: Empty answer submitted")
                _uiState.value = _uiState.value.copy(
                    error = "Please enter your answer"
                )
                return@launch
            }

            println("DEBUG: Starting answer evaluation")
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
                println("DEBUG: Answer evaluated")
                
                evaluation.fold(
                    onSuccess = { result ->
                        println("DEBUG: Processing evaluation result")
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
                        println("DEBUG: Saving answer to database")
                        repository.insertAnswer(answer)
                        
                        _uiState.value = _uiState.value.copy(
                            evaluation = result,
                            isEvaluatingAnswer = false
                        )
                        println("DEBUG: Answer submission completed successfully")
                        loadQuestions()
                    },
                    onFailure = { exception ->
                        println("DEBUG: Error evaluating answer: ${exception.message}")
                        _uiState.value = _uiState.value.copy(
                            isEvaluatingAnswer = false,
                            error = exception.message ?: "Failed to evaluate answer"
                        )
                    }
                )
            } catch (e: Exception) {
                println("DEBUG: Exception in submitAnswer: ${e.message}")
                e.printStackTrace()
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
        println("DEBUG: Setting difficulty level to $level")
        viewModelScope.launch {
            try {
                repository.setDifficultyLevel(level)
                _uiState.value = _uiState.value.copy(difficultyLevel = level)
                println("DEBUG: Difficulty level updated successfully")
            } catch (e: Exception) {
                println("DEBUG: Error setting difficulty level: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun setSelectedCategory(category: String) {
        println("DEBUG: Setting selected category to $category")
        viewModelScope.launch {
            try {
                repository.setSelectedCategory(category)
                _uiState.value = _uiState.value.copy(selectedCategory = category)
                println("DEBUG: Category updated successfully")
            } catch (e: Exception) {
                println("DEBUG: Error setting category: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun loadQuestions() {
        println("DEBUG: Loading questions")
        viewModelScope.launch {
            try {
                repository.getAllQuestionsWithAnswers().collect { questionsWithAnswers ->
                    _uiState.value = _uiState.value.copy(questionsWithAnswers = questionsWithAnswers)
                    println("DEBUG: Questions loaded successfully: ${questionsWithAnswers.size} items")
                }
            } catch (e: Exception) {
                println("DEBUG: Error loading questions: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun loadDifficultyLevel() {
        println("DEBUG: Loading difficulty level")
        viewModelScope.launch {
            try {
                val level = repository.getDifficultyLevel()
                _uiState.value = _uiState.value.copy(difficultyLevel = level)
                println("DEBUG: Difficulty level loaded: $level")
            } catch (e: Exception) {
                println("DEBUG: Error loading difficulty level: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun loadSelectedCategory() {
        println("DEBUG: Loading selected category")
        viewModelScope.launch {
            try {
                val category = repository.getSelectedCategory()
                _uiState.value = _uiState.value.copy(selectedCategory = category)
                println("DEBUG: Category loaded: $category")
            } catch (e: Exception) {
                println("DEBUG: Error loading category: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun loadLengthPreference() {
        println("DEBUG: Loading length preference")
        viewModelScope.launch {
            try {
                val length = repository.getLengthPreference()
                _uiState.value = _uiState.value.copy(lengthPreference = length)
                println("DEBUG: Length preference loaded: $length")
            } catch (e: Exception) {
                println("DEBUG: Error loading length preference: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun loadCategories() {
        println("DEBUG: Loading categories")
        viewModelScope.launch {
            try {
                repository.getAllCategories().collect { categories ->
                    _uiState.value = _uiState.value.copy(availableCategories = categories)
                    println("DEBUG: Categories loaded successfully: ${categories.size} items")
                }
            } catch (e: Exception) {
                println("DEBUG: Error loading categories: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    private fun updateUserProfile(userProfile: UserProfile?) {
        println("DEBUG: Updating user profile: ${userProfile?.name}")
        _uiState.value = _uiState.value.copy(userProfile = userProfile)
    }
}