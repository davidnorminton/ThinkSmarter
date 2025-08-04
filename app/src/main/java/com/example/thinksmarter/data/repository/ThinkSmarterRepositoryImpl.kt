package com.example.thinksmarter.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.thinksmarter.data.db.AppDatabase
import com.example.thinksmarter.data.model.Question
import com.example.thinksmarter.data.model.Answer
import com.example.thinksmarter.data.model.QuestionWithAnswer
import com.example.thinksmarter.data.model.Category
import com.example.thinksmarter.data.network.AnthropicApi
import com.example.thinksmarter.data.network.AnthropicRequest
import com.example.thinksmarter.data.network.Message
import com.example.thinksmarter.data.network.NetworkService
import com.example.thinksmarter.domain.repository.ThinkSmarterRepository
import com.example.thinksmarter.domain.repository.AnswerEvaluation
import com.example.thinksmarter.util.PromptTemplates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.regex.Pattern
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.thinksmarter.data.model.DailyChallenge
import com.example.thinksmarter.data.model.UserStreak

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThinkSmarterRepositoryImpl(
    private val context: Context,
    private val database: AppDatabase,
    private val anthropicApi: AnthropicApi = NetworkService.anthropicApi
) : ThinkSmarterRepository {

    private val questionDao = database.questionDao()
    private val answerDao = database.answerDao()
    private val categoryDao = database.categoryDao()
    private val dailyChallengeDao = database.dailyChallengeDao()

    // Question operations
    override fun getAllQuestions(): Flow<List<Question>> = questionDao.getAllQuestions()
    
    override fun getAllQuestionsWithAnswers(): Flow<List<QuestionWithAnswer>> = questionDao.getAllQuestionsWithAnswers()
    
    override suspend fun getQuestionById(questionId: Long): Question? = questionDao.getQuestionById(questionId)
    
    override suspend fun insertQuestion(question: Question): Long = questionDao.insertQuestion(question)
    
    override suspend fun deleteQuestion(question: Question) = questionDao.deleteQuestion(question)

    // Answer operations
    override fun getAnswersForQuestion(questionId: Long): Flow<List<Answer>> = answerDao.getAnswersForQuestion(questionId)
    
    override fun getAllAnswers(): Flow<List<Answer>> = answerDao.getAllAnswers()
    
    override suspend fun insertAnswer(answer: Answer): Long = answerDao.insertAnswer(answer)
    
    override suspend fun deleteAnswer(answer: Answer) = answerDao.deleteAnswer(answer)

    // Category operations
    override fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    
    override fun getDefaultCategories(): Flow<List<Category>> = categoryDao.getDefaultCategories()
    
    override fun getCustomCategories(): Flow<List<Category>> = categoryDao.getCustomCategories()
    
    override suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)
    
    override suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
    
    override suspend fun getCategoryByName(name: String): Category? = categoryDao.getCategoryByName(name)

    // Daily Challenge operations
    override suspend fun getDailyChallenge(date: String): DailyChallenge? = dailyChallengeDao.getDailyChallenge(date)
    
    override fun getRecentChallenges(): Flow<List<DailyChallenge>> = dailyChallengeDao.getRecentChallenges()
    
    override suspend fun createDailyChallenge(date: String, questionId: Long): DailyChallenge {
        val challenge = DailyChallenge(
            date = date,
            questionId = questionId,
            isCompleted = false
        )
        dailyChallengeDao.insertDailyChallenge(challenge)
        return challenge
    }
    
    override suspend fun completeDailyChallenge(date: String, answer: String, score: Int) {
        val challenge = getDailyChallenge(date)
        challenge?.let {
            val updatedChallenge = it.copy(
                isCompleted = true,
                userAnswer = answer,
                score = score
            )
            dailyChallengeDao.updateDailyChallenge(updatedChallenge)
            updateUserStreak()
        }
    }
    
    override suspend fun getUserStreak(): UserStreak? = dailyChallengeDao.getUserStreak()
    
    override suspend fun updateUserStreak() {
        val today = java.time.LocalDate.now().toString()
        val yesterday = java.time.LocalDate.now().minusDays(1).toString()
        
        val currentStreak = getUserStreak() ?: UserStreak()
        val todayChallenge = getDailyChallenge(today)
        val yesterdayChallenge = getDailyChallenge(yesterday)
        
        val newStreak = when {
            todayChallenge?.isCompleted == true -> {
                if (yesterdayChallenge?.isCompleted == true) {
                    currentStreak.copy(
                        currentStreak = currentStreak.currentStreak + 1,
                        longestStreak = maxOf(currentStreak.currentStreak + 1, currentStreak.longestStreak),
                        lastCompletedDate = today,
                        totalDaysCompleted = currentStreak.totalDaysCompleted + 1
                    )
                } else {
                    currentStreak.copy(
                        currentStreak = 1,
                        longestStreak = maxOf(1, currentStreak.longestStreak),
                        lastCompletedDate = today,
                        totalDaysCompleted = currentStreak.totalDaysCompleted + 1
                    )
                }
            }
            else -> currentStreak
        }
        
        dailyChallengeDao.insertUserStreak(newStreak)
    }

    // API operations
    override suspend fun generateQuestion(difficulty: Int, category: String, lengthPreference: String): Result<String> {
        return try {
            val apiKey = getApiKey() ?: return Result.failure(Exception("API key not set"))
            
            val prompt = PromptTemplates.generateQuestionPrompt(difficulty, category, lengthPreference)
            val request = AnthropicRequest(
                messages = listOf(Message("user", prompt))
            )
            
            val response = anthropicApi.generateQuestion(apiKey, request = request)
            val questionText = response.content.firstOrNull()?.text?.trim()
                ?: return Result.failure(Exception("Empty response from API"))
            
            Result.success(questionText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun evaluateAnswer(question: String, userAnswer: String, expectedLength: String): Result<AnswerEvaluation> {
        return try {
            val apiKey = getApiKey() ?: return Result.failure(Exception("API key not set"))
            
            val prompt = PromptTemplates.evaluateAnswerPrompt(question, userAnswer, expectedLength)
            val request = AnthropicRequest(
                messages = listOf(Message("user", prompt))
            )
            
            val response = anthropicApi.evaluateAnswer(apiKey, request = request)
            val responseText = response.content.firstOrNull()?.text
                ?: return Result.failure(Exception("Empty response from API"))
            
            val evaluation = parseEvaluationResponse(responseText)
            Result.success(evaluation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateFollowUpQuestions(originalQuestion: String, userAnswer: String): Result<List<String>> {
        return try {
            val apiKey = getApiKey() ?: return Result.failure(Exception("API key not set"))
            
            val prompt = PromptTemplates.generateFollowUpQuestionsPrompt(originalQuestion, userAnswer)
            val request = AnthropicRequest(
                messages = listOf(Message("user", prompt))
            )
            
            val response = anthropicApi.generateQuestion(apiKey, request = request)
            val responseText = response.content.firstOrNull()?.text
                ?: return Result.failure(Exception("Empty response from API"))
            
            val followUpQuestions = parseFollowUpQuestions(responseText)
            Result.success(followUpQuestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Settings operations
    override suspend fun getApiKey(): String? {
        val preferences = context.dataStore.data.first()
        val apiKey = preferences[PreferencesKeys.API_KEY]
        
        // Validate the stored API key
        if (apiKey != null && (apiKey.contains("🧱") || 
                               apiKey.contains("Functional Requirements") || 
                               apiKey.contains("MVP Scope") ||
                               apiKey.length < 20)) {
            // Clear the invalid API key
            clearApiKey()
            return null
        }
        
        return apiKey
    }

    override suspend fun setApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.API_KEY] = apiKey
        }
    }

    override suspend fun clearApiKey() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.API_KEY)
        }
    }

    override suspend fun getDifficultyLevel(): Int {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.DIFFICULTY_LEVEL] ?: 5
    }

    override suspend fun setDifficultyLevel(level: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DIFFICULTY_LEVEL] = level
        }
    }

    override suspend fun getSelectedCategory(): String {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.SELECTED_CATEGORY] ?: "General"
    }

    override suspend fun setSelectedCategory(category: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_CATEGORY] = category
        }
    }

    override suspend fun getLengthPreference(): String {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.LENGTH_PREFERENCE] ?: "Auto"
    }

    override suspend fun setLengthPreference(preference: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LENGTH_PREFERENCE] = preference
        }
    }

    override suspend fun getThemePreference(): String {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.THEME_PREFERENCE] ?: "Dark"
    }

    override suspend fun setThemePreference(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_PREFERENCE] = theme
        }
    }

    override suspend fun initializeDefaultCategories() {
        val defaultCategories = listOf(
            "Philosophical",
            "Leadership", 
            "Psychological",
            "Scientific",
            "Technological",
            "Society",
            "General"
        )
        
        for (categoryName in defaultCategories) {
            val existingCategory = getCategoryByName(categoryName)
            if (existingCategory == null) {
                insertCategory(Category(name = categoryName, isDefault = true))
            }
        }
    }

    private fun parseEvaluationResponse(responseText: String): AnswerEvaluation {
        val lines = responseText.split("\n")
        
        var clarityScore = 5
        var logicScore = 5
        var perspectiveScore = 5
        var depthScore = 5
        var feedback = ""
        var wordAndPhraseSuggestions = ""
        var betterAnswerSuggestions = ""
        var thoughtProcessGuidance = ""
        var modelAnswer = ""
        
        var currentSection = ""
        
        for (line in lines) {
            val trimmedLine = line.trim()
            when {
                trimmedLine.startsWith("CLARITY SCORE:") -> {
                    clarityScore = trimmedLine.substringAfter("CLARITY SCORE:").trim().substringBefore("[").trim().toIntOrNull() ?: 5
                }
                trimmedLine.startsWith("LOGIC SCORE:") -> {
                    logicScore = trimmedLine.substringAfter("LOGIC SCORE:").trim().substringBefore("[").trim().toIntOrNull() ?: 5
                }
                trimmedLine.startsWith("PERSPECTIVE SCORE:") -> {
                    perspectiveScore = trimmedLine.substringAfter("PERSPECTIVE SCORE:").trim().substringBefore("[").trim().toIntOrNull() ?: 5
                }
                trimmedLine.startsWith("DEPTH SCORE:") -> {
                    depthScore = trimmedLine.substringAfter("DEPTH SCORE:").trim().substringBefore("[").trim().toIntOrNull() ?: 5
                }
                trimmedLine.startsWith("FEEDBACK:") -> {
                    currentSection = "feedback"
                }
                trimmedLine.startsWith("WORD AND PHRASE SUGGESTIONS:") -> {
                    currentSection = "wordAndPhraseSuggestions"
                }
                trimmedLine.startsWith("BETTER ANSWER SUGGESTIONS:") -> {
                    currentSection = "betterAnswerSuggestions"
                }
                trimmedLine.startsWith("THOUGHT PROCESS GUIDANCE:") -> {
                    currentSection = "thoughtProcessGuidance"
                }
                trimmedLine.startsWith("MODEL ANSWER:") -> {
                    currentSection = "modelAnswer"
                }
                else -> {
                    when (currentSection) {
                        "feedback" -> feedback += if (feedback.isEmpty()) trimmedLine else "\n$trimmedLine"
                        "wordAndPhraseSuggestions" -> wordAndPhraseSuggestions += if (wordAndPhraseSuggestions.isEmpty()) trimmedLine else "\n$trimmedLine"
                        "betterAnswerSuggestions" -> betterAnswerSuggestions += if (betterAnswerSuggestions.isEmpty()) trimmedLine else "\n$trimmedLine"
                        "thoughtProcessGuidance" -> thoughtProcessGuidance += if (thoughtProcessGuidance.isEmpty()) trimmedLine else "\n$trimmedLine"
                        "modelAnswer" -> modelAnswer += if (modelAnswer.isEmpty()) trimmedLine else "\n$trimmedLine"
                    }
                }
            }
        }
        
        return AnswerEvaluation(
            clarityScore = clarityScore,
            logicScore = logicScore,
            perspectiveScore = perspectiveScore,
            depthScore = depthScore,
            feedback = feedback.trim(),
            wordAndPhraseSuggestions = wordAndPhraseSuggestions.trim(),
            betterAnswerSuggestions = betterAnswerSuggestions.trim(),
            thoughtProcessGuidance = thoughtProcessGuidance.trim(),
            modelAnswer = modelAnswer.trim()
        )
    }

    private fun parseFollowUpQuestions(responseText: String): List<String> {
        val questionPattern = Pattern.compile("\\d+\\.\\s*(.*?)\\s*\\(Score:\\s*\\d+\\)")
        val questions = mutableListOf<String>()
        var matcher = questionPattern.matcher(responseText)
        
        while (matcher.find()) {
            questions.add(matcher.group(1).trim())
        }
        return questions
    }

    private object PreferencesKeys {
        val API_KEY = stringPreferencesKey("api_key")
        val DIFFICULTY_LEVEL = intPreferencesKey("difficulty_level")
        val SELECTED_CATEGORY = stringPreferencesKey("selected_category")
        val LENGTH_PREFERENCE = stringPreferencesKey("length_preference")
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
    }
} 