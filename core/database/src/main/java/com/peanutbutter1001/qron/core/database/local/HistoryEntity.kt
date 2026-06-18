package com.peanutbutter1001.qron.core.database.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.peanutbutter1001.qron.domain.model.QRType
import com.peanutbutter1001.qron.domain.model.ScanSource

@Entity(tableName = "history_table")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rawValue: String,
    val title: String,
    val type: QRType,
    val source: ScanSource,
    val scannedAtTimestamp: Long
)
