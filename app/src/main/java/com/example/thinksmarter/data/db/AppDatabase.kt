package com.example.thinksmarter.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.thinksmarter.data.model.Question
import com.example.thinksmarter.data.model.Answer
import com.example.thinksmarter.data.model.Category
import com.example.thinksmarter.data.model.DailyChallenge
import com.example.thinksmarter.data.model.UserStreak
import com.example.thinksmarter.data.model.TextImprovement

@Database(
    entities = [Question::class, Answer::class, Category::class, DailyChallenge::class, UserStreak::class, TextImprovement::class],
    version = 1, // Reset to version 1
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun answerDao(): AnswerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun dailyChallengeDao(): DailyChallengeDao
    abstract fun textImprovementDao(): TextImprovementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                println("DEBUG: Creating database instance")
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "thinksmarter_database_v1" // New database name
                    )
                    .fallbackToDestructiveMigration() // This will recreate the database if schema changes
                    .build()
                    println("DEBUG: Database instance created successfully")
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    println("DEBUG: Error creating database instance: ${e.message}")
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }
}