package com.example.thinksmarter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "random_facts")
data class RandomFact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val fact: String,
    val timestamp: LocalDateTime
) 