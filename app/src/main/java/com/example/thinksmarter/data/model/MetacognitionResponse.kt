package com.example.thinksmarter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "metacognition_responses")
data class MetacognitionResponse(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userInput: String,
    val guidance: String,
    val timestamp: LocalDateTime
) 