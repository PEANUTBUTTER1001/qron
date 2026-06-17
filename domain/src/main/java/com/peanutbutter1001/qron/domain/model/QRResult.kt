package com.peanutbutter1001.qron.domain.model

import java.time.LocalDateTime

enum class QRType {
    URL, WIFI, CONTACT, TEXT, PAYMENT, UNKNOWN
}

enum class ScanSource {
    SCREEN, EXTERNAL
}

data class QRResult(
    val id: Long = 0,
    val rawValue: String,
    val title: String,
    val type: QRType,
    val source: ScanSource,
    val scannedAt: LocalDateTime = LocalDateTime.now()
)
