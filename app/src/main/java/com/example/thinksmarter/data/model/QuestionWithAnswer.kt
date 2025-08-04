package com.example.thinksmarter.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class QuestionWithAnswer(
    @Embedded
    val question: Question,
    @Relation(
        parentColumn = "id",
        entityColumn = "questionId"
    )
    val answer: Answer?
) 