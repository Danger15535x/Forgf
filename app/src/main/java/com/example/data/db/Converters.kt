package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.TranscriptMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, TranscriptMessage::class.java)
    private val adapter = moshi.adapter<List<TranscriptMessage>>(listType)

    @TypeConverter
    fun fromString(value: String): List<TranscriptMessage> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            try {
                adapter.fromJson(value) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    @TypeConverter
    fun fromList(list: List<TranscriptMessage>): String {
        return adapter.toJson(list)
    }
}
