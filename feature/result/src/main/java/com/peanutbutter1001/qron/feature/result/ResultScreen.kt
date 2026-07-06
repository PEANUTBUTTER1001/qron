package com.peanutbutter1001.qron.feature.result

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Image
import com.peanutbutter1001.qron.domain.model.QRResult
import com.peanutbutter1001.qron.domain.model.QRType
import com.peanutbutter1001.qron.domain.model.ScanSource
import java.time.LocalDateTime

// ──────────────────────────────────────────────────────────────
// Stateful 계층 (Route)
// ──────────────────────────────────────────────────────────────

/**
 * 결과 화면의 stateful 계층.
 * - ViewModel 보유 및 상태 수집
 * - 네비게이션 인자(resultId) 로드
 * - 링크 열기/복사/공유 같은 플랫폼 동작을 Context로 수행하여 Screen에 콜백으로 전달
 * - ViewModel의 snackbarMessage를 collect해 SnackbarHostState에 반영
 * - dismiss 시 qrImageCache evict 호출
 */
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun ResultRoute(
    resultId: Long,
    onDismiss: () -> Unit,
    viewModel: ScanResultViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(resultId) { viewModel.load(resultId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { resId ->
            snackbarHostState.showSnackbar(context.getString(resId))
        }
    }

    ResultScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onDismiss = {
            // 화면이 닫힐 때 캐시에서 Bitmap 제거 → recycle()
            viewModel.evictImage(resultId)
            onDismiss()
        },
        onOpenLink = { url ->
            context.openUrl(url, onError = { viewModel.showSnackbar(R.string.result_error_cannot_open_link) })
        },
        onCopy = { text ->
            context.copyToClipboard(text)
            viewModel.showSnackbar(R.string.result_copied_to_clipboard)
        },
        onShare = { text ->
            context.shareText(text, context.getString(R.string.result_share_chooser_title))
        }
    )
}

private fun Context.openUrl(url: String, onError: () -> Unit) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (e: Exception) {
        onError()
    }
}

private fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", text))
}

private fun Context.shareText(text: String, chooserTitle: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, chooserTitle))
}

// ──────────────────────────────────────────────────────────────
// Stateless 계층 (Screen)
// ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    uiState: ResultUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onDismiss: () -> Unit,
    onOpenLink: (String) -> Unit = {},
    onCopy: (String) -> Unit = {},
    onShare: (String) -> Unit = {}
) {
    // skipPartiallyExpanded = true: 뒤로가기 한 번에 완전히 닫힘.
    // false(기본값)이면 PartiallyExpanded → Hidden 2단계로 뒤로가기가 두 번 필요하다.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(snackbarData = data)
        }
        when (uiState) {
            is ResultUiState.Loading -> CenteredBox { CircularProgressIndicator() }
            is ResultUiState.NotFound -> CenteredBox { Text(stringResource(R.string.result_error_cannot_open_link)) }
            is ResultUiState.Success -> ResultContent(
                qrResult = uiState.qrResult,
                croppedBitmap = uiState.croppedBitmap,
                onOpenLink = { onOpenLink(uiState.qrResult.rawValue) },
                onCopy = { onCopy(uiState.qrResult.rawValue) },
                onShare = { onShare(uiState.qrResult.rawValue) }
            )
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) { content() }
}

// ──────────────────────────────────────────────────────────────
// 콘텐츠 컴포저블
// ──────────────────────────────────────────────────────────────

@Composable
fun ResultContent(
    qrResult: QRResult,
    croppedBitmap: Bitmap? = null,
    onOpenLink: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // 인식된 QR 코드 원본 이미지 영역.
        // 스캔 직후에만 존재하며, History 재진입 시에는 null이므로 미표시한다.
        if (croppedBitmap != null) {
            Image(
                bitmap = croppedBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(qrResult.type.name)
        Spacer(modifier = Modifier.height(8.dp))
        Text(qrResult.title)
        Spacer(modifier = Modifier.height(4.dp))
        Text(qrResult.rawValue)

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val isUrl = qrResult.type == QRType.URL
            if (isUrl) {
                Button(onClick = onOpenLink, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.result_action_open_link))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            IconButton(
                onClick = onCopy,
                modifier = if (!isUrl) Modifier.weight(1f) else Modifier
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.result_action_copy),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onShare,
                modifier = if (!isUrl) Modifier.weight(1f) else Modifier
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = stringResource(R.string.result_action_share),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultContentPreview() {
    MaterialTheme {
        Surface {
            ResultContent(
                qrResult = QRResult(
                    id = 1,
                    rawValue = "https://example.com",
                    title = "예시 링크",
                    type = QRType.URL,
                    source = ScanSource.EXTERNAL,
                    scannedAt = LocalDateTime.of(2024, 1, 1, 12, 0)
                ),
                croppedBitmap = null,
                onOpenLink = {},
                onCopy = {},
                onShare = {}
            )
        }
    }
}
