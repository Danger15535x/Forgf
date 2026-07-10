package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_sessions")
data class CallSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val transcriptJson: String // Serialized List<TranscriptMessage>
)
