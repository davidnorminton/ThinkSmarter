package com.example.thinksmarter.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.thinksmarter.data.model.Question
import com.example.thinksmarter.data.model.Answer
import com.example.thinksmarter.data.model.Category
import com.example.thinksmarter.data.model.DailyChallenge
import com.example.thinksmarter.data.model.UserStreak
import com.example.thinksmarter.data.model.TextImprovement
import com.example.thinksmarter.data.model.RandomFact

@Database(
    entities = [Question::class, Answer::class, Category::class, DailyChallenge::class, UserStreak::class, TextImprovement::class, RandomFact::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun answerDao(): AnswerDao
    abstract fun categoryDao(): CategoryDao
    abstract fun dailyChallengeDao(): DailyChallengeDao
    abstract fun textImprovementDao(): TextImprovementDao
    abstract fun randomFactDao(): RandomFactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the random_facts table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `random_facts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `category` TEXT NOT NULL,
                        `fact` TEXT NOT NULL,
                        `timestamp` INTEGER
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                println("DEBUG: Creating database instance")
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "thinksmarter_database_v1"
                    )
                    .addMigrations(MIGRATION_1_2)
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