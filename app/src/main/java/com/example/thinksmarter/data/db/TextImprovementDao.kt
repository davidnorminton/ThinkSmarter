package com.example.thinksmarter.data.db

import androidx.room.*
import com.example.thinksmarter.data.model.TextImprovement
import kotlinx.coroutines.flow.Flow

@Dao
interface TextImprovementDao {
    @Insert
    suspend fun insertTextImprovement(textImprovement: TextImprovement): Long

    @Query("SELECT * FROM text_improvements ORDER BY timestamp DESC")
    fun getAllTextImprovements(): Flow<List<TextImprovement>>

    @Query("SELECT * FROM text_improvements WHERE id = :id")
    suspend fun getTextImprovementById(id: Long): TextImprovement?

    @Delete
    suspend fun deleteTextImprovement(textImprovement: TextImprovement)

    @Query("DELETE FROM text_improvements")
    suspend fun deleteAllTextImprovements()

    @Query("SELECT COUNT(*) FROM text_improvements")
    suspend fun getTextImprovementCount(): Int

    @Query("SELECT AVG(clarityScore + logicScore + perspectiveScore + depthScore) / 4.0 FROM text_improvements")
    suspend fun getAverageTextImprovementScore(): Double?
}