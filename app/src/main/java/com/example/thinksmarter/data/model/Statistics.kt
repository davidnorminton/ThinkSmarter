package com.example.thinksmarter.data.model

data class Statistics(
    val totalQuestions: Int = 0,
    val totalAnswers: Int = 0,
    val averageClarity: Float = 0f,
    val averageLogic: Float = 0f,
    val averagePerspective: Float = 0f,
    val averageDepth: Float = 0f,
    val overallAverage: Float = 0f,
    val bestCategory: String = "None",
    val worstCategory: String = "None",
    val improvementTrend: Float = 0f,
    val categoryStats: Map<String, CategoryStats> = emptyMap(),
    val commonThoughtProcesses: List<String> = emptyList()
)

data class CategoryStats(
    val category: String,
    val questionCount: Int,
    val averageScore: Float,
    val clarityAverage: Float,
    val logicAverage: Float,
    val perspectiveAverage: Float,
    val depthAverage: Float
) 