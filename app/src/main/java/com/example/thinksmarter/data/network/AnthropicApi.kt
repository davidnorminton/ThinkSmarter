package com.example.thinksmarter.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AnthropicApi {
    @POST("v1/messages")
    suspend fun generateQuestion(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: AnthropicRequest
    ): AnthropicResponse

    @POST("v1/messages")
    suspend fun evaluateAnswer(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: AnthropicRequest
    ): AnthropicResponse
} 