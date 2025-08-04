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

@Database(
    entities = [Question::class, Answer::class, Category::class, DailyChallenge::class, UserStreak::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun answerDao(): AnswerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun dailyChallengeDao(): DailyChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "thinksmarter_database"
                )
                .fallbackToDestructiveMigration() // This will recreate the database if schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 