package com.example.thinksmarter.data.model

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class QuestionWithAnswer(
    @Embedded
    val question: Question,
    @Relation(
        parentColumn = "id",
        entityColumn = "questionId"
    )
    val answer: Answer?
) : Parcelable 