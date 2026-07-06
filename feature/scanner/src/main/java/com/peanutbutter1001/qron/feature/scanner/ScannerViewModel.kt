package com.peanutbutter1001.qron.feature.scanner

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peanutbutter1001.qron.core.vision.QRImageCache
import com.peanutbutter1001.qron.core.vision.QRScannerDataSource
import com.peanutbutter1001.qron.domain.model.ScanSource
import com.peanutbutter1001.qron.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scannerDataSource: QRScannerDataSource,
    private val historyRepository: HistoryRepository,
    private val qrImageCache: QRImageCache
) : ViewModel() {

    private val _scanResultEvent = MutableSharedFlow<Long>()
    val scanResultEvent = _scanResultEvent.asSharedFlow()

    private var isProcessing = false

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    fun processImageProxy(imageProxy: ImageProxy) {
        if (isProcessing) {
            imageProxy.close()
            return
        }

        isProcessing = true

        // 회전 정보를 toBitmap() 이전에 읽는다.
        // toBitmap()은 YUV→ARGB 변환만 수행하며 rotation을 적용하지 않으므로
        // crop 후 별도로 보정해야 한다.
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val bitmap = imageProxy.toBitmap()

        viewModelScope.launch {
            try {
                val scanResults = scannerDataSource.scanBitmap(bitmap, ScanSource.EXTERNAL)
                if (scanResults.isNotEmpty()) {
                    val first = scanResults.first()
                    val id = historyRepository.saveResult(first.qrResult)

                    // QR 인식 영역을 padding 포함해 crop한 뒤 화면 표시 방향으로 회전해 캐시에 저장.
                    // boundingBox가 없는 경우(드물지만 ML Kit이 null 반환 가능)는 이미지 없이 진행.
                    first.boundingBox?.cropWithPadding(bitmap, padding = 40)
                        ?.rotateIfNeeded(rotationDegrees)
                        ?.let { qrImageCache.put(id, it) }

                    _scanResultEvent.emit(id)
                    // Note: 결과를 보여주는 동안 추가 스캔 방지를 위해 isProcessing을 false로 돌리지 않음
                } else {
                    isProcessing = false
                }
            } finally {
                // 예외 여부와 무관하게 원본 Bitmap 즉시 해제
                bitmap.recycle()
                imageProxy.close()
            }
        }
    }

    fun resumeScanning() {
        isProcessing = false
    }
}

/**
 * boundingBox 주변에 padding을 추가해 crop한 Bitmap을 반환한다.
 * 원본 Bitmap 경계를 벗어나지 않도록 clamp 처리한다.
 */
private fun Rect.cropWithPadding(source: Bitmap, padding: Int): Bitmap {
    val left   = (left   - padding).coerceAtLeast(0)
    val top    = (top    - padding).coerceAtLeast(0)
    val right  = (right  + padding).coerceAtMost(source.width)
    val bottom = (bottom + padding).coerceAtMost(source.height)
    return Bitmap.createBitmap(source, left, top, right - left, bottom - top)
}

/**
 * 카메라 센서 방향과 화면 표시 방향의 차이를 보정한다.
 * degrees == 0 이면 원본 Bitmap을 그대로 반환(새 객체 생성 없음).
 * 회전이 필요한 경우 원본은 즉시 recycle해 메모리를 해제한다.
 */
private fun Bitmap.rotateIfNeeded(degrees: Int): Bitmap {
    if (degrees == 0) return this
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        .also { rotated -> if (rotated !== this) recycle() }
}
