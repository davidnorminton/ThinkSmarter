package com.example.thinksmarter.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnthropicRequest(
    val model: String = "claude-3-7-sonnet-latest",
    val max_tokens: Int = 1000,
    val messages: List<Message>
)

@JsonClass(generateAdapter = true)
data class Message(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class AnthropicResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<Content>,
    val model: String,
    @Json(name = "stop_reason")
    val stopReason: String?,
    @Json(name = "stop_sequence")
    val stopSequence: String?,
    val usage: Usage
)

@JsonClass(generateAdapter = true)
data class Content(
    val type: String,
    val text: String
)

@JsonClass(generateAdapter = true)
data class Usage(
    @Json(name = "input_tokens")
    val inputTokens: Int,
    @Json(name = "output_tokens")
    val outputTokens: Int
) 