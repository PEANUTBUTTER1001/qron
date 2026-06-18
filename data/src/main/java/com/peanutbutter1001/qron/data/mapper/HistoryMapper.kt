package com.peanutbutter1001.qron.data.mapper

import com.peanutbutter1001.qron.core.database.local.HistoryEntity
import com.peanutbutter1001.qron.domain.model.QRResult
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun HistoryEntity.toDomain(): QRResult = QRResult(
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

fun QRResult.toEntity(): HistoryEntity = HistoryEntity(
    id = id,
    rawValue = rawValue,
    title = title,
    type = type,
    source = source,
    scannedAtTimestamp = scannedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
)
