package com.peanutbutter1001.qron.core.vision

import android.graphics.Rect
import com.peanutbutter1001.qron.domain.model.QRResult

/**
 * QR 스캔 결과 + ML Kit이 감지한 바코드 위치(boundingBox).
 * boundingBox는 원본 Bitmap 좌표계 기준이며, crop에 사용된 후 QRResult만 domain으로 전달된다.
 * domain 모듈에 Android 타입(Rect)을 노출하지 않기 위해 core:vision 계층에 선언한다.
 */
data class QRScanResult(
    val qrResult: QRResult,
    val boundingBox: Rect?
)
