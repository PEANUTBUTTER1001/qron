package com.peanutbutter1001.qron.data.scanner

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.peanutbutter1001.qron.domain.model.QRResult
import com.peanutbutter1001.qron.domain.model.QRType
import com.peanutbutter1001.qron.domain.model.ScanSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class QRScannerDataSource @Inject constructor() {
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    suspend fun scanBitmap(bitmap: Bitmap, source: ScanSource): List<QRResult> {
        val image = InputImage.fromBitmap(bitmap, 0)
        return scanInputImage(image, source)
    }

    suspend fun scanInputImage(image: InputImage, source: ScanSource): List<QRResult> {
        return try {
            val barcodes = scanner.process(image).await()
            barcodes.mapNotNull { mapBarcodeToQRResult(it, source) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun mapBarcodeToQRResult(barcode: Barcode, source: ScanSource): QRResult? {
        val rawValue = barcode.rawValue ?: return null
        
        val type = when (barcode.valueType) {
            Barcode.TYPE_URL -> QRType.URL
            Barcode.TYPE_WIFI -> QRType.WIFI
            Barcode.TYPE_CONTACT_INFO -> QRType.CONTACT
            Barcode.TYPE_TEXT -> QRType.TEXT
            else -> QRType.UNKNOWN
        }

        val title = when (type) {
            QRType.URL -> barcode.url?.title ?: rawValue
            QRType.WIFI -> barcode.wifi?.ssid ?: "WiFi Network"
            QRType.CONTACT -> barcode.contactInfo?.name?.formattedName ?: "Contact"
            else -> rawValue
        }

        return QRResult(
            rawValue = rawValue,
            title = title,
            type = type,
            source = source
        )
    }
}
