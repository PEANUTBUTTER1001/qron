package com.peanutbutter1001.qron.data.repository

import com.peanutbutter1001.qron.core.database.local.HistoryDao
import com.peanutbutter1001.qron.data.mapper.toDomain
import com.peanutbutter1001.qron.data.mapper.toEntity
import com.peanutbutter1001.qron.domain.model.QRResult
import com.peanutbutter1001.qron.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : HistoryRepository {

    override fun getAllHistory(): Flow<List<QRResult>> {
        return historyDao.getAllHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getResultById(id: Long): QRResult? {
        return historyDao.getHistoryById(id)?.toDomain()
    }

    override suspend fun saveResult(result: QRResult): Long {
        return historyDao.insertHistory(result.toEntity())
    }

    override suspend fun deleteResult(id: Long) {
        historyDao.deleteHistory(id)
    }

    override suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }
}
