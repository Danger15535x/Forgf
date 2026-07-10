package com.example.data.repository

import com.example.data.db.CallSession
import com.example.data.db.CallSessionDao
import kotlinx.coroutines.flow.Flow

class CallSessionRepository(private val dao: CallSessionDao) {
    val allSessions: Flow<List<CallSession>> = dao.getAllSessions()

    suspend fun insertSession(session: CallSession): Long {
        return dao.insertSession(session)
    }

    suspend fun deleteSessionById(id: Int) {
        dao.deleteSessionById(id)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}
