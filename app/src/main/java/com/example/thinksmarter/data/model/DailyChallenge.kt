package com.example.thinksmarter.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_challenges",
    foreignKeys = [
        ForeignKey(
            entity = Question::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index(value = ["questionId"]),
        androidx.room.Index(value = ["date"])
    ]
)
data class DailyChallenge(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD format
    val questionId: Long,
    val isCompleted: Boolean = false,
    val userAnswer: String? = null,
    val score: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_streaks")
data class UserStreak(
    @PrimaryKey
    val id: Int = 1, // Only one streak record per user
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: String? = null,
    val totalDaysCompleted: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) 