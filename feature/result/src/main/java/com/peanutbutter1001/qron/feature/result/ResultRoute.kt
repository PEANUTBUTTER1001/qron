package com.peanutbutter1001.qron.feature.result

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 결과 화면의 stateful 계층.
 * - ViewModel 보유 및 상태 수집
 * - 네비게이션 인자(resultId) 로드
 * - 링크 열기/복사/공유 같은 플랫폼 동작을 Context로 수행하여 Screen에 콜백으로 전달
 */
@Composable
fun ResultRoute(
    resultId: Long,
    onDismiss: () -> Unit,
    viewModel: ScanResultViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(resultId) { viewModel.load(resultId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ResultScreen(
        uiState = uiState,
        onDismiss = onDismiss,
        onOpenLink = { url -> context.openUrl(url) },
        onCopy = { text -> context.copyToClipboard(text) },
        onShare = { text -> context.shareText(text) }
    )
}

private fun Context.openUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: Exception) {
        Toast.makeText(this, "열 수 없는 링크입니다.", Toast.LENGTH_SHORT).show()
    }
}

private fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", text))
    Toast.makeText(this, "클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
}

private fun Context.shareText(text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, "공유하기"))
}
