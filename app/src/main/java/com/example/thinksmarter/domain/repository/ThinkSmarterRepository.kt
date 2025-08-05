package com.example.thinksmarter.domain.repository

import com.example.thinksmarter.data.model.Question
import com.example.thinksmarter.data.model.Answer
import com.example.thinksmarter.data.model.QuestionWithAnswer
import com.example.thinksmarter.data.model.Category
import com.example.thinksmarter.data.model.DailyChallenge
import com.example.thinksmarter.data.model.UserStreak
import com.example.thinksmarter.data.model.TextImprovement
import kotlinx.coroutines.flow.Flow

interface ThinkSmarterRepository {
    // Question operations
    fun getAllQuestions(): Flow<List<Question>>
    fun getAllQuestionsWithAnswers(): Flow<List<QuestionWithAnswer>>
    suspend fun getQuestionById(questionId: Long): Question?
    suspend fun insertQuestion(question: Question): Long
    suspend fun deleteQuestion(question: Question)
    
    // Answer operations
    fun getAnswersForQuestion(questionId: Long): Flow<List<Answer>>
    fun getAllAnswers(): Flow<List<Answer>>
    suspend fun insertAnswer(answer: Answer): Long
    suspend fun deleteAnswer(answer: Answer)
    
    // Category operations
    fun getAllCategories(): Flow<List<Category>>
    fun getDefaultCategories(): Flow<List<Category>>
    fun getCustomCategories(): Flow<List<Category>>
    suspend fun insertCategory(category: Category): Long
    suspend fun deleteCategory(category: Category)
    suspend fun getCategoryByName(name: String): Category?
    
    // Daily Challenge operations
    suspend fun getDailyChallenge(date: String): DailyChallenge?
    fun getRecentChallenges(): Flow<List<DailyChallenge>>
    suspend fun createDailyChallenge(date: String, questionId: Long): DailyChallenge
    suspend fun completeDailyChallenge(date: String, answer: String, score: Int)
    suspend fun getUserStreak(): UserStreak?
    suspend fun updateUserStreak()
    
    // API operations
    suspend fun generateQuestion(difficulty: Int, category: String = "General", lengthPreference: String = "Auto"): Result<String>
    suspend fun evaluateAnswer(question: String, userAnswer: String, expectedLength: String): Result<AnswerEvaluation>
    suspend fun generateFollowUpQuestions(originalQuestion: String, userAnswer: String): Result<List<String>>
    
    // Text Improvement
    suspend fun improveText(userText: String, textType: String): Result<AnswerEvaluation>
    suspend fun insertTextImprovement(textImprovement: TextImprovement): Long
    suspend fun getAllTextImprovements(): Flow<List<TextImprovement>>
    suspend fun deleteTextImprovement(textImprovement: TextImprovement)
    suspend fun getTextImprovementCount(): Int
    suspend fun getAverageTextImprovementScore(): Double?
    
    // Settings operations
    suspend fun getApiKey(): String?
    suspend fun setApiKey(apiKey: String)
    suspend fun clearApiKey()
    suspend fun getDifficultyLevel(): Int
    suspend fun setDifficultyLevel(level: Int)
    suspend fun getSelectedCategory(): String
    suspend fun setSelectedCategory(category: String)
    suspend fun getLengthPreference(): String
    suspend fun setLengthPreference(preference: String)
    suspend fun getThemePreference(): String
    suspend fun setThemePreference(theme: String)
    
    // Initialization operations
    suspend fun initializeDefaultCategories()
    
    // Random facts
    suspend fun generateRandomFact(category: String): Result<String>
}

data class AnswerEvaluation(
    val clarityScore: Int,
    val logicScore: Int,
    val perspectiveScore: Int,
    val depthScore: Int,
    val feedback: String,
    val wordAndPhraseSuggestions: String,
    val betterAnswerSuggestions: String,
    val thoughtProcessGuidance: String,
    val modelAnswer: String
) 