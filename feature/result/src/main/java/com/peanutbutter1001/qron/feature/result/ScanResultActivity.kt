package com.peanutbutter1001.qron.feature.result

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.peanutbutter1001.qron.core.designsystem.theme.QRonTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 앱 외부(OS) 진입 전용 결과 화면.
 * 화면 내 스캔(feature:scan)의 QuickSettings 타일 → 접근성 서비스 캡처 후
 * PendingIntent로 이 Activity를 실행하며, "QR_RESULT_ID" extra로 결과 ID를 전달한다.
 *
 * 인앱 스캔 전환은 이 Activity가 아니라 AppNavHost의 Result destination을 사용한다.
 * 두 경로 모두 동일한 ResultRoute/ResultScreen 컴포저블을 재사용한다.
 */
@AndroidEntryPoint
class ScanResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val resultId = intent.getLongExtra(EXTRA_QR_RESULT_ID, -1L)
        setContent {
            QRonTheme {
                ResultRoute(
                    resultId = resultId,
                    onDismiss = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_QR_RESULT_ID = "QR_RESULT_ID"
    }
}
