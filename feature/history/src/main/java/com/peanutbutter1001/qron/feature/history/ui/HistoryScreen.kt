package com.peanutbutter1001.qron.feature.history.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peanutbutter1001.qron.core.designsystem.theme.QRonTheme
import com.peanutbutter1001.qron.domain.model.QRResult
import com.peanutbutter1001.qron.domain.model.QRType
import com.peanutbutter1001.qron.domain.model.ScanSource
import com.peanutbutter1001.qron.feature.history.R
import com.peanutbutter1001.qron.feature.history.util.scanSourceToString
import com.peanutbutter1001.qron.feature.history.util.toDisplayString
import java.time.LocalDateTime

/** 기록 화면의 stateful 계층. ViewModel 상태를 수집해 stateless Screen에 전달한다. */
@Composable
fun HistoryRoute(viewModel: HistoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(
        uiState = uiState,
        onFilterSelected = viewModel::onFilterSelected,
        onClearHistory = viewModel::clearHistory
    )
}

/**
 * 기록 화면의 stateless 계층. 상태(uiState)와 이벤트 콜백만 받는다.
 */
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onFilterSelected: (HistoryFilter) -> Unit,
    onClearHistory: () -> Unit
) {
    // 전체 삭제 확인 다이얼로그 노출 여부(일회성 UI 상태이므로 로컬 remember로 관리).
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 검색은 향후 구현 예정 (placeholder)
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.history_search_hint)) }
            )
            IconButton(onClick = { showClearDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.history_clear_content_desc)
                )
            }
        }

        LazyRow(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(HistoryFilter.entries) { filter ->
                FilterChip(
                    selected = uiState.selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(filterLabel(filter)) }
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.items) { result ->
                HistoryItem(result)
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.history_clear_dialog_title)) },
            text = { Text(stringResource(R.string.history_clear_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearHistory()
                    }
                ) { Text(stringResource(R.string.history_clear_dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.history_clear_dialog_cancel))
                }
            }
        )
    }
}

/** HistoryFilter → 표시 문자열 매핑(문자열 리소스). */
@Composable
private fun filterLabel(filter: HistoryFilter): String = when (filter) {
    HistoryFilter.ALL -> stringResource(R.string.history_filter_all)
    HistoryFilter.SCAN -> stringResource(R.string.scan_source_screen)
    HistoryFilter.CAMERA -> stringResource(R.string.scan_source_external)
}

@Composable
private fun HistoryItem(result: QRResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            ExpandableText(text = result.rawValue)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = scanSourceToString(result.source))
                Spacer(modifier = Modifier.weight(1f))
                Text(text = result.scannedAt.toDisplayString())
            }
        }
    }
}

@Composable
private fun ExpandableText(
    text: String,
    maxLines: Int = 2
) {
    var expanded by remember { mutableStateOf(false) }

    Text(
        modifier = Modifier.clickable { expanded = !expanded },
        text = text,
        maxLines = if (expanded) Int.MAX_VALUE else maxLines,
        overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis
    )
}

@Preview(name = "목록 있음")
@Composable
private fun HistoryScreenPreview() {
    QRonTheme {
        HistoryScreen(
            uiState = HistoryUiState(
                items = listOf(
                    QRResult(
                        id = 1,
                        rawValue = "https://example.com",
                        title = "예시 링크",
                        type = QRType.URL,
                        source = ScanSource.EXTERNAL,
                        scannedAt = LocalDateTime.now()
                    )
                ),
                selectedFilter = HistoryFilter.ALL
            ),
            onFilterSelected = {},
            onClearHistory = {}
        )
    }
}

@Preview(name = "빈 목록")
@Composable
private fun HistoryScreenEmptyPreview() {
    QRonTheme {
        HistoryScreen(
            uiState = HistoryUiState(),
            onFilterSelected = {},
            onClearHistory = {}
        )
    }
}
