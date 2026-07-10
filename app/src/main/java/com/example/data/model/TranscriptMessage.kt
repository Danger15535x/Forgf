package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TranscriptMessage(
    val sender: String, // "Speaker A", "You" (TTS spoken), "AI Suggested"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuggested: Boolean = false
)
