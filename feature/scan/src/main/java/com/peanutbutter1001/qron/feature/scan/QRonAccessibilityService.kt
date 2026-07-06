package com.peanutbutter1001.qron.feature.scan

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.peanutbutter1001.qron.core.vision.QRImageCache
import com.peanutbutter1001.qron.core.vision.QRScannerDataSource
import com.peanutbutter1001.qron.domain.model.ScanSource
import com.peanutbutter1001.qron.domain.repository.HistoryRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QRonAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "QRonAccessibility"
    }

    @Inject lateinit var scannerDataSource: QRScannerDataSource
    @Inject lateinit var historyRepository: HistoryRepository
    @Inject lateinit var qrImageCache: QRImageCache

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        Log.e(TAG, "코루틴 예외", e)
    }
    private val serviceScope =
        CoroutineScope(Dispatchers.Main + SupervisorJob() + exceptionHandler)

    private var currentPendingIntent: android.app.PendingIntent? = null

    private val captureReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == QRonTileService.ACTION_REQUEST_SCREENSHOT) {
                currentPendingIntent = intent.getParcelableExtra("PENDING_RESULT_INTENT")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    performScreenshot()
                } else {
                    Log.e(TAG, "Screenshot requires API 30+")
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val filter = IntentFilter(QRonTileService.ACTION_REQUEST_SCREENSHOT)
        ContextCompat.registerReceiver(
            this, captureReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { unregisterReceiver(captureReceiver) }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    @RequiresApi(Build.VERSION_CODES.R)
    private fun performScreenshot() {
        serviceScope.launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
                } else {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    kotlinx.coroutines.delay(300)
                    performGlobalAction(GLOBAL_ACTION_BACK)
                }

                kotlinx.coroutines.delay(600)

                takeScreenshot(
                    Display.DEFAULT_DISPLAY,
                    mainExecutor,
                    object : TakeScreenshotCallback {
                        override fun onSuccess(screenshotResult: ScreenshotResult) {
                            try {
                                val hardwareBuffer = screenshotResult.hardwareBuffer
                                val colorSpace = screenshotResult.colorSpace
                                val hwBitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)
                                hardwareBuffer.close()

                                if (hwBitmap != null) {
                                    // 하드웨어 비트맵은 ML Kit에서 픽셀 접근이 불가하므로 소프트웨어 비트맵으로 복사한다.
                                    val softwareBitmap = hwBitmap.copy(Bitmap.Config.ARGB_8888, false)
                                    hwBitmap.recycle()
                                    processBitmap(softwareBitmap)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "스크린샷 처리 실패", e)
                            }
                        }

                        override fun onFailure(errorCode: Int) {
                            Log.e(TAG, "스크린샷 실패 code=$errorCode")
                            serviceScope.launch {
                                android.widget.Toast.makeText(
                                    this@QRonAccessibilityService,
                                    "화면 캡처 실패 (코드: $errorCode).",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "performScreenshot 실패", e)
            }
        }
    }

    private fun processBitmap(bitmap: Bitmap) {
        serviceScope.launch {
            try {
                val scanResults = scannerDataSource.scanBitmap(bitmap, ScanSource.SCREEN)
                if (scanResults.isNotEmpty()) {
                    val first = scanResults.first()
                    val id = historyRepository.saveResult(first.qrResult)

                    // QR 인식 영역 crop 후 캐시 저장.
                    // try 블록 안에서 처리하고 finally에서 원본을 항상 recycle한다.
                    first.boundingBox?.cropWithPadding(bitmap, padding = 40)
                        ?.let { qrImageCache.put(id, it) }

                    val fillInIntent = Intent().apply { putExtra("QR_RESULT_ID", id) }
                    currentPendingIntent?.send(this@QRonAccessibilityService, 0, fillInIntent)
                } else {
                    android.widget.Toast.makeText(
                        this@QRonAccessibilityService,
                        "화면에서 QR 코드를 찾을 수 없습니다.",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "processBitmap 실패", e)
            } finally {
                // 기존 누락 버그 수정: 예외 여부와 무관하게 원본 스크린샷 Bitmap 항상 해제
                bitmap.recycle()
            }
        }
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
