package com.example.thinksmarter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "text_improvements")
data class TextImprovement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalText: String,
    val textType: String,
    val clarityScore: Int,
    val logicScore: Int,
    val perspectiveScore: Int,
    val depthScore: Int,
    val feedback: String,
    val wordAndPhraseSuggestions: String,
    val betterAnswerSuggestions: String,
    val thoughtProcessGuidance: String,
    val modelAnswer: String,
    val timestamp: Long = System.currentTimeMillis()
)