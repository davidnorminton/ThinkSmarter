package com.example.thinksmarter.data.db

import androidx.room.*
import com.example.thinksmarter.data.model.DailyChallenge
import com.example.thinksmarter.data.model.UserStreak
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyChallengeDao {
    @Query("SELECT * FROM daily_challenges WHERE date = :date LIMIT 1")
    suspend fun getDailyChallenge(date: String): DailyChallenge?

    @Query("SELECT * FROM daily_challenges ORDER BY date DESC LIMIT 30")
    fun getRecentChallenges(): Flow<List<DailyChallenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyChallenge(challenge: DailyChallenge): Long

    @Update
    suspend fun updateDailyChallenge(challenge: DailyChallenge)

    @Query("SELECT COUNT(*) FROM daily_challenges WHERE isCompleted = 1")
    suspend fun getCompletedChallengesCount(): Int

    @Query("SELECT * FROM user_streaks WHERE id = 1 LIMIT 1")
    suspend fun getUserStreak(): UserStreak?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStreak(streak: UserStreak)

    @Update
    suspend fun updateUserStreak(streak: UserStreak)
} 