package com.peanutbutter1001.qron.core.vision

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

    /**
     * Bitmap으로 스캔. QRScanResult(QRResult + boundingBox)를 반환해
     * 호출부에서 crop 처리를 할 수 있도록 한다.
     */
    suspend fun scanBitmap(bitmap: Bitmap, source: ScanSource): List<QRScanResult> {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val barcodes = scanner.process(image).await()
            barcodes.mapNotNull { mapBarcodeToScanResult(it, source) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * InputImage로 스캔. CameraX ImageProxy 이외의 경로(테스트 등)에서 사용.
     * boundingBox 없이 QRResult만 반환한다.
     */
    suspend fun scanInputImage(image: InputImage, source: ScanSource): List<QRResult> {
        return try {
            val barcodes = scanner.process(image).await()
            barcodes.mapNotNull { mapBarcodeToQRResult(it, source) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun mapBarcodeToScanResult(barcode: Barcode, source: ScanSource): QRScanResult? {
        val qrResult = mapBarcodeToQRResult(barcode, source) ?: return null
        return QRScanResult(
            qrResult = qrResult,
            boundingBox = barcode.boundingBox
        )
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
