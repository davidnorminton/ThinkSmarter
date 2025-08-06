package com.example.thinksmarter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val difficulty: Int, // 1-10
    val category: String = "General", // Default category
    val expectedAnswerLength: String = "Medium", // Short, Medium, Long
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable 