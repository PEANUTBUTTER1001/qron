package com.peanutbutter1001.qron.core.database.local

import androidx.room.TypeConverter
import com.peanutbutter1001.qron.domain.model.QRType
import com.peanutbutter1001.qron.domain.model.ScanSource

class Converters {
    @TypeConverter
    fun fromQRType(value: QRType): String {
        return value.name
    }

    @TypeConverter
    fun toQRType(value: String): QRType {
        return try {
            QRType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            QRType.UNKNOWN
        }
    }

    @TypeConverter
    fun fromScanSource(value: ScanSource): String {
        return value.name
    }

    @TypeConverter
    fun toScanSource(value: String): ScanSource {
        return try {
            ScanSource.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ScanSource.EXTERNAL
        }
    }
}
