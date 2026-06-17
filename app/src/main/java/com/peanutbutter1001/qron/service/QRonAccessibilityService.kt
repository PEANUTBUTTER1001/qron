package com.peanutbutter1001.qron.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.hardware.HardwareBuffer
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import com.peanutbutter1001.qron.data.scanner.QRScannerDataSource
import com.peanutbutter1001.qron.domain.model.ScanSource
import com.peanutbutter1001.qron.domain.repository.HistoryRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QRonAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var scannerDataSource: QRScannerDataSource

    @Inject
    lateinit var historyRepository: HistoryRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main)

    private var currentPendingIntent: android.app.PendingIntent? = null

    private val captureReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == QRonTileService.ACTION_REQUEST_SCREENSHOT) {
                currentPendingIntent = intent.getParcelableExtra("PENDING_RESULT_INTENT")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    performScreenshot()
                } else {
                    Log.e("QRon", "Screenshot requires API 30+")
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val filter = IntentFilter(QRonTileService.ACTION_REQUEST_SCREENSHOT)
        androidx.core.content.ContextCompat.registerReceiver(this, captureReceiver, filter, androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(captureReceiver)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    @RequiresApi(Build.VERSION_CODES.R)
    private fun performScreenshot() {
        serviceScope.launch {
            // 상단바 닫기
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // API 31 이상에서는 전용 액션 사용
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
            } else {
                performGlobalAction(GLOBAL_ACTION_BACK)
                kotlinx.coroutines.delay(300)
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
            
            // 애니메이션 대기
            kotlinx.coroutines.delay(600)

            takeScreenshot(
                Display.DEFAULT_DISPLAY,
                mainExecutor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshotResult: ScreenshotResult) {
                        val hardwareBuffer = screenshotResult.hardwareBuffer
                        val colorSpace = screenshotResult.colorSpace
                        val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)
                        hardwareBuffer.close()

                        if (bitmap != null) {
                            processBitmap(bitmap)
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        Log.e("QRon", "Screenshot failed: $errorCode")
                        serviceScope.launch {
                            android.widget.Toast.makeText(this@QRonAccessibilityService, "화면 캡처 실패 (코드: $errorCode). 상단바가 완전히 닫혔는지 확인해주세요.", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
        }
    }

    private fun processBitmap(bitmap: Bitmap) {
        serviceScope.launch {
            val results = scannerDataSource.scanBitmap(bitmap, ScanSource.SCREEN)
            if (results.isNotEmpty()) {
                val result = results.first()
                val id = historyRepository.saveResult(result)
                
                try {
                    val fillInIntent = Intent().apply { putExtra("QR_RESULT_ID", id) }
                    currentPendingIntent?.send(this@QRonAccessibilityService, 0, fillInIntent)
                } catch (e: Exception) {
                    Log.e("QRon", "Failed to send pending intent", e)
                }
            } else {
                Log.d("QRon", "No QR found")
                android.widget.Toast.makeText(this@QRonAccessibilityService, "화면에서 QR 코드를 찾을 수 없습니다.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
