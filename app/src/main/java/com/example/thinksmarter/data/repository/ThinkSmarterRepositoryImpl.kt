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
import com.example.thinksmarter.data.model.TextImprovement
import com.example.thinksmarter.data.model.RandomFact
import com.example.thinksmarter.data.network.Message

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
    private val randomFactDao = database.randomFactDao()

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
            val apiKey = getApiKey() ?: return Result.failure(Exception("API key not configured"))
            
            val prompt = PromptTemplates.generateFollowUpQuestionsPrompt(originalQuestion, userAnswer)
            val request = AnthropicRequest(
                model = "claude-3-7-sonnet-latest",
                max_tokens = 1000,
                messages = listOf(
                    Message(
                        role = "user",
                        content = prompt
                    )
                )
            )
            
            val response = anthropicApi.generateQuestion(apiKey, request = request)
            val questions = parseFollowUpQuestions(response.content[0].text)
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun improveText(userText: String, textType: String): Result<AnswerEvaluation> {
        println("DEBUG: Starting text improvement")
        return try {
            val apiKey = getApiKey() ?: return Result.failure(Exception("API key not configured"))
            println("DEBUG: API key retrieved")
            
            val prompt = PromptTemplates.improveTextPrompt(userText, textType)
            println("DEBUG: Generated prompt")
            
            val request = AnthropicRequest(
                model = "claude-3-7-sonnet-latest",
                max_tokens = 2000,
                messages = listOf(
                    Message(
                        role = "user",
                        content = prompt
                    )
                )
            )
            println("DEBUG: Created API request")
            
            val response = anthropicApi.generateQuestion(apiKey, request = request)
            println("DEBUG: Received API response")
            println("DEBUG: Raw response text: ${response.content[0].text}")
            
            val evaluation = parseEvaluationResponse(response.content[0].text)
            println("DEBUG: Parsed evaluation: $evaluation")
            println("DEBUG: Feedback length: ${evaluation.feedback.length}")
            println("DEBUG: Word suggestions length: ${evaluation.wordAndPhraseSuggestions.length}")
            println("DEBUG: Better answer suggestions length: ${evaluation.betterAnswerSuggestions.length}")
            println("DEBUG: Thought process guidance length: ${evaluation.thoughtProcessGuidance.length}")
            println("DEBUG: Model answer length: ${evaluation.modelAnswer.length}")
            
            Result.success(evaluation)
        } catch (e: Exception) {
            println("DEBUG: Error in improveText: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    override suspend fun insertTextImprovement(textImprovement: TextImprovement): Long {
        return database.textImprovementDao().insertTextImprovement(textImprovement)
    }
    
    override suspend fun getAllTextImprovements(): Flow<List<TextImprovement>> {
        try {
            return database.textImprovementDao().getAllTextImprovements()
        } catch (e: Exception) {
            println("DEBUG: Error getting text improvements: ${e.message}")
            e.printStackTrace()
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }
    
    override suspend fun deleteTextImprovement(textImprovement: TextImprovement) {
        database.textImprovementDao().deleteTextImprovement(textImprovement)
    }
    
    override suspend fun getTextImprovementCount(): Int {
        return database.textImprovementDao().getTextImprovementCount()
    }
    
    override suspend fun getAverageTextImprovementScore(): Double? {
        return database.textImprovementDao().getAverageTextImprovementScore()
    }
    
    private fun parseEvaluationResponse(responseText: String): AnswerEvaluation {
        println("DEBUG: Starting response parsing")
        println("DEBUG: Full response text:")
        println(responseText)
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
            println("DEBUG: Processing line: '$trimmedLine'")
            when {
                trimmedLine.startsWith("CLARITY SCORE:") -> {
                    println("DEBUG: Found clarity score line: $trimmedLine")
                    clarityScore = trimmedLine.substringAfter("CLARITY SCORE:").trim().substringBefore("[").trim().toIntOrNull() ?: 5
                }
                trimmedLine.startsWith("LOGIC SCORE:") -> {
                    println("DEBUG: Found logic score line: $trimmedLine")
                    logicScore = trimmedLine.substringAfter("LOGIC SCORE:").trim().substringBefore("[").trim().toIntOrNull() ?: 5
                }
                trimmedLine.startsWith("PERSPECTIVE SCORE:") -> {
                    println("DEBUG: Found perspective score line: $trimmedLine")
                    perspectiveScore = trimmedLine.substringAfter("PERSPECTIVE SCORE:").trim().substringBefore("[").trim().toIntOrNull() ?: 5
                }
                trimmedLine.startsWith("DEPTH SCORE:") -> {
                    println("DEBUG: Found depth score line: $trimmedLine")
                    depthScore = trimmedLine.substringAfter("DEPTH SCORE:").trim().substringBefore("[").trim().toIntOrNull() ?: 5
                }
                trimmedLine.startsWith("FEEDBACK:") || trimmedLine.startsWith("FEEDBACK") || trimmedLine.contains("FEEDBACK") -> {
                    println("DEBUG: Starting feedback section")
                    currentSection = "feedback"
                }
                trimmedLine.startsWith("WORD AND PHRASE SUGGESTIONS:") || trimmedLine.startsWith("WORD AND PHRASE SUGGESTIONS") || trimmedLine.startsWith("WORD & PHRASE SUGGESTIONS:") || trimmedLine.startsWith("WORD & PHRASE SUGGESTIONS") || trimmedLine.contains("WORD") && trimmedLine.contains("PHRASE") -> {
                    println("DEBUG: Starting word and phrase suggestions section")
                    currentSection = "wordAndPhraseSuggestions"
                }
                trimmedLine.startsWith("BETTER ANSWER SUGGESTIONS:") || trimmedLine.startsWith("BETTER ANSWER SUGGESTIONS") || trimmedLine.startsWith("IMPROVEMENT SUGGESTIONS:") || trimmedLine.startsWith("IMPROVEMENT SUGGESTIONS") || trimmedLine.contains("BETTER") && trimmedLine.contains("SUGGESTIONS") -> {
                    println("DEBUG: Starting better answer suggestions section")
                    currentSection = "betterAnswerSuggestions"
                }
                trimmedLine.startsWith("THOUGHT PROCESS GUIDANCE:") || trimmedLine.startsWith("THOUGHT PROCESS GUIDANCE") || trimmedLine.startsWith("WRITING PROCESS GUIDANCE:") || trimmedLine.startsWith("WRITING PROCESS GUIDANCE") || trimmedLine.contains("PROCESS") && trimmedLine.contains("GUIDANCE") -> {
                    println("DEBUG: Starting thought process guidance section")
                    currentSection = "thoughtProcessGuidance"
                }
                trimmedLine.startsWith("MODEL ANSWER:") || trimmedLine.startsWith("MODEL ANSWER") || trimmedLine.startsWith("IMPROVED VERSION:") || trimmedLine.startsWith("IMPROVED VERSION") || trimmedLine.contains("MODEL") && trimmedLine.contains("ANSWER") -> {
                    println("DEBUG: Starting model answer section")
                    currentSection = "modelAnswer"
                }
                else -> {
                    if (trimmedLine.isNotEmpty()) {
                        println("DEBUG: Adding to section '$currentSection': '$trimmedLine'")
                        when (currentSection) {
                            "feedback" -> {
                                feedback += if (feedback.isEmpty()) trimmedLine else "\n$trimmedLine"
                            }
                            "wordAndPhraseSuggestions" -> {
                                wordAndPhraseSuggestions += if (wordAndPhraseSuggestions.isEmpty()) trimmedLine else "\n$trimmedLine"
                            }
                            "betterAnswerSuggestions" -> {
                                betterAnswerSuggestions += if (betterAnswerSuggestions.isEmpty()) trimmedLine else "\n$trimmedLine"
                            }
                            "thoughtProcessGuidance" -> {
                                thoughtProcessGuidance += if (thoughtProcessGuidance.isEmpty()) trimmedLine else "\n$trimmedLine"
                            }
                            "modelAnswer" -> {
                                modelAnswer += if (modelAnswer.isEmpty()) trimmedLine else "\n$trimmedLine"
                            }
                            else -> {
                                // If no section is detected, add to feedback as fallback
                                if (feedback.isEmpty() && !trimmedLine.startsWith("CLARITY SCORE:") && !trimmedLine.startsWith("LOGIC SCORE:") && !trimmedLine.startsWith("PERSPECTIVE SCORE:") && !trimmedLine.startsWith("DEPTH SCORE:")) {
                                    println("DEBUG: Adding to feedback as fallback: '$trimmedLine'")
                                    feedback += if (feedback.isEmpty()) trimmedLine else "\n$trimmedLine"
                                }
                            }
                        }
                    }
                }
            }
        }
        
        println("DEBUG: Parsing completed")
        println("DEBUG: Feedback length: ${feedback.length}")
        println("DEBUG: Word suggestions length: ${wordAndPhraseSuggestions.length}")
        println("DEBUG: Better answer suggestions length: ${betterAnswerSuggestions.length}")
        println("DEBUG: Thought process guidance length: ${thoughtProcessGuidance.length}")
        println("DEBUG: Model answer length: ${modelAnswer.length}")
        println("DEBUG: Raw response text (first 500 chars): ${responseText.take(500)}")
        
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
    
    override suspend fun generateRandomFact(category: String): Result<String> {
        return try {
            println("DEBUG: Generating random fact for category: $category")
            
            val apiKey = getApiKey()
            if (apiKey == null) {
                return Result.failure(Exception("API key not configured"))
            }
            
            val prompt = PromptTemplates.generateRandomFactPrompt(category)
            println("DEBUG: Using prompt: $prompt")
            
            val request = AnthropicRequest(
                model = "claude-3-7-sonnet-latest",
                max_tokens = 500,
                messages = listOf(Message(role = "user", content = prompt))
            )
            
            val response = anthropicApi.generateQuestion(apiKey, request = request)
            println("DEBUG: Random fact generated successfully")
            
            Result.success(response.content.first().text)
        } catch (e: Exception) {
            println("DEBUG: Error generating random fact: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun saveRandomFact(fact: RandomFact) {
        randomFactDao.insertFact(fact)
    }

    override fun getRandomFactsByCategory(category: String): Flow<List<RandomFact>> {
        return randomFactDao.getFactsByCategory(category)
    }

    override fun getAllRandomFacts(): Flow<List<RandomFact>> {
        return randomFactDao.getAllFacts()
    }

    private object PreferencesKeys {
        val API_KEY = stringPreferencesKey("api_key")
        val DIFFICULTY_LEVEL = intPreferencesKey("difficulty_level")
        val SELECTED_CATEGORY = stringPreferencesKey("selected_category")
        val LENGTH_PREFERENCE = stringPreferencesKey("length_preference")
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
    }
} 