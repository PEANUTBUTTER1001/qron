package com.peanutbutter1001.qron.feature.scanner

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
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
    private val historyRepository: HistoryRepository
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
        
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            isProcessing = true
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            viewModelScope.launch {
                val results = scannerDataSource.scanInputImage(image, ScanSource.EXTERNAL)
                if (results.isNotEmpty()) {
                    // QR 인식 성공
                    val result = results.first()
                    val id = historyRepository.saveResult(result)
                    _scanResultEvent.emit(id)
                    // Note: 결과를 보여주는 동안 추가 스캔 방지를 위해 isProcessing을 false로 돌리지 않음 (UI가 재개될 때 초기화)
                } else {
                    isProcessing = false
                }
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }

    fun resumeScanning() {
        isProcessing = false
    }
}
