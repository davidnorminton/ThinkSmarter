package com.example.thinksmarter.data.db

import androidx.room.*
import com.example.thinksmarter.data.model.Answer
import kotlinx.coroutines.flow.Flow

@Dao
interface AnswerDao {
    @Query("SELECT * FROM answers WHERE questionId = :questionId")
    fun getAnswersForQuestion(questionId: Long): Flow<List<Answer>>

    @Query("SELECT * FROM answers ORDER BY timestamp DESC")
    fun getAllAnswers(): Flow<List<Answer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: Answer): Long

    @Delete
    suspend fun deleteAnswer(answer: Answer)

    @Query("DELETE FROM answers WHERE questionId = :questionId")
    suspend fun deleteAnswersForQuestion(questionId: Long)

    @Query("DELETE FROM answers")
    suspend fun deleteAllAnswers()
} 