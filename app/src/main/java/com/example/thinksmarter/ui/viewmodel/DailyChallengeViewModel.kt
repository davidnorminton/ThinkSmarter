package com.example.thinksmarter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinksmarter.data.model.DailyChallenge
import com.example.thinksmarter.data.model.UserStreak
import com.example.thinksmarter.data.model.Question
import com.example.thinksmarter.domain.repository.ThinkSmarterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DailyChallengeUiState(
    val todayChallenge: DailyChallenge? = null,
    val todayQuestion: Question? = null,
    val userStreak: UserStreak? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isGeneratingChallenge: Boolean = false
)

sealed class DailyChallengeUiEvent {
    object LoadDailyChallenge : DailyChallengeUiEvent()
    object GenerateDailyChallenge : DailyChallengeUiEvent()
    data class SubmitDailyAnswer(val answer: String) : DailyChallengeUiEvent()
}

class DailyChallengeViewModel(
    private val repository: ThinkSmarterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyChallengeUiState())
    val uiState: StateFlow<DailyChallengeUiState> = _uiState.asStateFlow()

    init {
        loadDailyChallenge()
        loadUserStreak()
    }

    fun handleEvent(event: DailyChallengeUiEvent) {
        when (event) {
            is DailyChallengeUiEvent.LoadDailyChallenge -> loadDailyChallenge()
            is DailyChallengeUiEvent.GenerateDailyChallenge -> generateDailyChallenge()
            is DailyChallengeUiEvent.SubmitDailyAnswer -> submitDailyAnswer(event.answer)
        }
    }

    private fun loadDailyChallenge() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val today = LocalDate.now().toString()
                val challenge = repository.getDailyChallenge(today)
                
                if (challenge != null) {
                    val question = repository.getQuestionById(challenge.questionId)
                    _uiState.value = _uiState.value.copy(
                        todayChallenge = challenge,
                        todayQuestion = question,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load daily challenge"
                )
            }
        }
    }

    private fun generateDailyChallenge() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingChallenge = true, error = null)
            
            try {
                val questionText = repository.generateQuestion(5, "General", "Medium")
                questionText.fold(
                    onSuccess = { text ->
                        val question = Question(
                            text = text,
                            difficulty = 5,
                            category = "General",
                            expectedAnswerLength = "Medium"
                        )
                        val questionId = repository.insertQuestion(question)
                        val savedQuestion = question.copy(id = questionId)
                        
                        val today = LocalDate.now().toString()
                        val challenge = repository.createDailyChallenge(today, questionId)
                        
                        _uiState.value = _uiState.value.copy(
                            todayChallenge = challenge,
                            todayQuestion = savedQuestion,
                            isGeneratingChallenge = false
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isGeneratingChallenge = false,
                            error = exception.message ?: "Failed to generate daily challenge"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingChallenge = false,
                    error = e.message ?: "Failed to generate daily challenge"
                )
            }
        }
    }

    private fun submitDailyAnswer(answer: String) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now().toString()
                val challenge = _uiState.value.todayChallenge
                val question = _uiState.value.todayQuestion
                
                if (challenge != null && question != null) {
                    val evaluation = repository.evaluateAnswer(question.text, answer, question.expectedAnswerLength)
                    evaluation.fold(
                        onSuccess = { result ->
                            val score = (result.clarityScore + result.logicScore + result.perspectiveScore + result.depthScore) / 4
                            repository.completeDailyChallenge(today, answer, score)
                            
                            // Save the answer
                            val answerEntity = com.example.thinksmarter.data.model.Answer(
                                questionId = question.id,
                                userAnswer = answer,
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
                            repository.insertAnswer(answerEntity)
                            
                            loadUserStreak()
                        },
                        onFailure = { exception ->
                            _uiState.value = _uiState.value.copy(
                                error = exception.message ?: "Failed to evaluate answer"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to submit answer"
                )
            }
        }
    }

    private fun loadUserStreak() {
        viewModelScope.launch {
            try {
                val streak = repository.getUserStreak()
                _uiState.value = _uiState.value.copy(userStreak = streak)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
} 