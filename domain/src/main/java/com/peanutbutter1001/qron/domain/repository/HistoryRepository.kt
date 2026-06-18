package com.peanutbutter1001.qron.domain.repository

import com.peanutbutter1001.qron.domain.model.QRResult
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getAllHistory(): Flow<List<QRResult>>
    suspend fun getResultById(id: Long): QRResult?
    suspend fun saveResult(result: QRResult): Long
    suspend fun deleteResult(id: Long)
    suspend fun clearHistory()
}
