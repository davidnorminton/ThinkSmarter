package com.example.thinksmarter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinksmarter.data.model.Statistics
import com.example.thinksmarter.data.model.CategoryStats
import com.example.thinksmarter.domain.repository.ThinkSmarterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class StatisticsUiState(
    val statistics: Statistics = Statistics(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class StatisticsViewModel(
    private val repository: ThinkSmarterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        // Load statistics after a delay to ensure database is ready
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            loadStatistics()
        }
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Use first() instead of collect() to get a single value
                val questionsWithAnswers = repository.getAllQuestionsWithAnswers().first()
                val statistics = calculateStatistics(questionsWithAnswers)
                
                _uiState.value = _uiState.value.copy(
                    statistics = statistics,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load statistics"
                )
            }
        }
    }

    private fun calculateStatistics(questionsWithAnswers: List<com.example.thinksmarter.data.model.QuestionWithAnswer>): Statistics {
        val answers = questionsWithAnswers.mapNotNull { it.answer }
        
        if (answers.isEmpty()) {
            return Statistics()
        }

        val totalQuestions = questionsWithAnswers.size
        val totalAnswers = answers.size
        
        val averageClarity = answers.map { it.clarityScore }.average().toFloat()
        val averageLogic = answers.map { it.logicScore }.average().toFloat()
        val averagePerspective = answers.map { it.perspectiveScore }.average().toFloat()
        val averageDepth = answers.map { it.depthScore }.average().toFloat()
        val overallAverage = (averageClarity + averageLogic + averagePerspective + averageDepth) / 4f

        // Calculate category statistics
        val categoryStats = mutableMapOf<String, MutableList<com.example.thinksmarter.data.model.Answer>>()
        
        questionsWithAnswers.forEach { qwa ->
            val category = qwa.question.category
            qwa.answer?.let { answer ->
                categoryStats.getOrPut(category) { mutableListOf() }.add(answer)
            }
        }

        val categoryStatsMap = categoryStats.mapValues { (category, answers) ->
            val clarityAvg = answers.map { it.clarityScore }.average().toFloat()
            val logicAvg = answers.map { it.logicScore }.average().toFloat()
            val perspectiveAvg = answers.map { it.perspectiveScore }.average().toFloat()
            val depthAvg = answers.map { it.depthScore }.average().toFloat()
            val avgScore = (clarityAvg + logicAvg + perspectiveAvg + depthAvg) / 4f
            
            CategoryStats(
                category = category,
                questionCount = answers.size,
                averageScore = avgScore,
                clarityAverage = clarityAvg,
                logicAverage = logicAvg,
                perspectiveAverage = perspectiveAvg,
                depthAverage = depthAvg
            )
        }

        // Find best and worst categories
        val bestCategory = categoryStatsMap.maxByOrNull { it.value.averageScore }?.key ?: "None"
        val worstCategory = categoryStatsMap.minByOrNull { it.value.averageScore }?.key ?: "None"

        // Calculate improvement trend (simple: compare first half vs second half)
        val sortedAnswers = answers.sortedBy { it.timestamp }
        val halfPoint = sortedAnswers.size / 2
        val firstHalf = sortedAnswers.take(halfPoint)
        val secondHalf = sortedAnswers.drop(halfPoint)
        
        val firstHalfAvg = if (firstHalf.isNotEmpty()) {
            firstHalf.map { (it.clarityScore + it.logicScore + it.perspectiveScore + it.depthScore) / 4f }.average().toFloat()
        } else 0f
        
        val secondHalfAvg = if (secondHalf.isNotEmpty()) {
            secondHalf.map { (it.clarityScore + it.logicScore + it.perspectiveScore + it.depthScore) / 4f }.average().toFloat()
        } else 0f
        
        val improvementTrend = secondHalfAvg - firstHalfAvg

        return Statistics(
            totalQuestions = totalQuestions,
            totalAnswers = totalAnswers,
            averageClarity = averageClarity,
            averageLogic = averageLogic,
            averagePerspective = averagePerspective,
            averageDepth = averageDepth,
            overallAverage = overallAverage,
            bestCategory = bestCategory,
            worstCategory = worstCategory,
            improvementTrend = improvementTrend,
            categoryStats = categoryStatsMap
        )
    }
} 