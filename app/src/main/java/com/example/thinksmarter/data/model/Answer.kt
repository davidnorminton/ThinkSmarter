package com.example.thinksmarter.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
@Entity(
    tableName = "answers",
    foreignKeys = [
        ForeignKey(
            entity = Question::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index(value = ["questionId"])
    ]
)
data class Answer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: Long,
    val userAnswer: String,
    val clarityScore: Int, // 1-10
    val logicScore: Int, // 1-10
    val perspectiveScore: Int, // 1-10
    val depthScore: Int, // 1-10
    val feedback: String,
    val wordAndPhraseSuggestions: String,
    val betterAnswerSuggestions: String,
    val thoughtProcessGuidance: String,
    val modelAnswer: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable 