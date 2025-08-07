package com.example.thinksmarter.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.thinksmarter.data.model.MetacognitionResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface MetacognitionResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(response: MetacognitionResponse)

    @Query("SELECT * FROM metacognition_responses ORDER BY timestamp DESC")
    fun getAllResponses(): Flow<List<MetacognitionResponse>>

    @Query("DELETE FROM metacognition_responses")
    suspend fun deleteAllResponses()
} 