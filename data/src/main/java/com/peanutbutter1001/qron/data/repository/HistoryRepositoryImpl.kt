package com.peanutbutter1001.qron.data.repository

import com.peanutbutter1001.qron.data.local.HistoryDao
import com.peanutbutter1001.qron.data.local.HistoryEntity
import com.peanutbutter1001.qron.domain.model.QRResult
import com.peanutbutter1001.qron.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
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

    private fun HistoryEntity.toDomain() = QRResult(
        id = id,
        rawValue = rawValue,
        title = title,
        type = type,
        source = source,
        scannedAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(scannedAtTimestamp), 
            ZoneId.systemDefault()
        )
    )

    private fun QRResult.toEntity() = HistoryEntity(
        id = id,
        rawValue = rawValue,
        title = title,
        type = type,
        source = source,
        scannedAtTimestamp = scannedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
}
