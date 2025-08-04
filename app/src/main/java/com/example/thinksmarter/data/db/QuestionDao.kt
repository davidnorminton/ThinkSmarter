package com.example.thinksmarter.data.db

import androidx.room.*
import com.example.thinksmarter.data.model.Question
import com.example.thinksmarter.data.model.QuestionWithAnswer
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY timestamp DESC")
    fun getAllQuestions(): Flow<List<Question>>

    @Transaction
    @Query("SELECT * FROM questions ORDER BY timestamp DESC")
    fun getAllQuestionsWithAnswers(): Flow<List<QuestionWithAnswer>>

    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: Long): Question?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long

    @Delete
    suspend fun deleteQuestion(question: Question)

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()
} 