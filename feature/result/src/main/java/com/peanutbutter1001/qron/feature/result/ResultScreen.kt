package com.peanutbutter1001.qron.feature.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.peanutbutter1001.qron.domain.model.QRResult
import com.peanutbutter1001.qron.domain.model.QRType
import com.peanutbutter1001.qron.domain.model.ScanSource
import java.time.LocalDateTime

/**
 * 결과 화면의 stateless 계층. 바텀시트 형태로 결과를 표시한다.
 * 테마는 상위 QRonTheme를 그대로 상속한다(기존 MaterialTheme 하드코딩 제거).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    uiState: ResultUiState,
    onDismiss: () -> Unit,
    onOpenLink: (String) -> Unit = {},
    onCopy: (String) -> Unit = {},
    onShare: (String) -> Unit = {}
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        when (uiState) {
            is ResultUiState.Loading -> CenteredBox { CircularProgressIndicator() }
            is ResultUiState.NotFound -> CenteredBox { Text("결과를 찾을 수 없습니다.") }
            is ResultUiState.Success -> ResultContent(
                qrResult = uiState.qrResult,
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

@Composable
fun ResultContent(
    qrResult: QRResult,
    onOpenLink: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
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
                    Text("Open Link")
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            OutlinedButton(
                onClick = onCopy,
                modifier = if (!isUrl) Modifier.weight(1f) else Modifier
            ) { Text("Copy") }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onShare,
                modifier = if (!isUrl) Modifier.weight(1f) else Modifier
            ) { Text("Share") }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview
@Composable
private fun ResultContentPreview() {
    ResultContent(
        qrResult = QRResult(
            id = 1,
            rawValue = "https://example.com",
            title = "예시 링크",
            type = QRType.URL,
            source = ScanSource.EXTERNAL,
            scannedAt = LocalDateTime.now()
        ),
        onOpenLink = {},
        onCopy = {},
        onShare = {}
    )
}
