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
import com.example.thinksmarter.data.model.MetacognitionResponse

@Database(
    entities = [Question::class, Answer::class, Category::class, DailyChallenge::class, UserStreak::class, TextImprovement::class, RandomFact::class, MetacognitionResponse::class],
    version = 5,
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
    abstract fun metacognitionResponseDao(): MetacognitionResponseDao

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the metacognition_responses table with nullable timestamp (old version)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `metacognition_responses` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userInput` TEXT NOT NULL,
                        `guidance` TEXT NOT NULL,
                        `timestamp` INTEGER
                    )
                """)
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Fix the timestamp column to be NOT NULL
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `metacognition_responses_fixed` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userInput` TEXT NOT NULL,
                        `guidance` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """)
                
                // Copy data from old table to new table
                db.execSQL("""
                    INSERT INTO metacognition_responses_fixed (id, userInput, guidance, timestamp)
                    SELECT id, userInput, guidance, COALESCE(timestamp, 0) FROM metacognition_responses
                """)
                
                // Drop old table and rename new table
                db.execSQL("DROP TABLE metacognition_responses")
                db.execSQL("ALTER TABLE metacognition_responses_fixed RENAME TO metacognition_responses")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Fix the random_facts timestamp column to be NOT NULL
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `random_facts_fixed` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `category` TEXT NOT NULL,
                        `fact` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """)
                
                // Copy data from old table to new table
                db.execSQL("""
                    INSERT INTO random_facts_fixed (id, category, fact, timestamp)
                    SELECT id, category, fact, COALESCE(timestamp, 0) FROM random_facts
                """)
                
                // Drop old table and rename new table
                db.execSQL("DROP TABLE random_facts")
                db.execSQL("ALTER TABLE random_facts_fixed RENAME TO random_facts")
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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