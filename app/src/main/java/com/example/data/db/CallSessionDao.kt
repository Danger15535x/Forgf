package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallSessionDao {
    @Query("SELECT * FROM call_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<CallSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CallSession): Long

    @Query("DELETE FROM call_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    @Query("DELETE FROM call_sessions")
    suspend fun deleteAll()
}
