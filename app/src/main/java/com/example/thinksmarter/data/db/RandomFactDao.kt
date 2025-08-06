package com.example.thinksmarter.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.thinksmarter.data.model.RandomFact
import kotlinx.coroutines.flow.Flow

@Dao
interface RandomFactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFact(fact: RandomFact)

    @Query("SELECT * FROM random_facts WHERE category = :category ORDER BY timestamp DESC")
    fun getFactsByCategory(category: String): Flow<List<RandomFact>>

    @Query("SELECT * FROM random_facts ORDER BY timestamp DESC")
    fun getAllFacts(): Flow<List<RandomFact>>

    @Query("DELETE FROM random_facts")
    suspend fun deleteAllFacts()
} 