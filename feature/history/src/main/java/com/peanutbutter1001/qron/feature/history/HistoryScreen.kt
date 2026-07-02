package com.peanutbutter1001.qron.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
 * 기록 화면의 stateless 계층. 상태(uiState)와 이벤트 콜백만 받는다.
 */
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onFilterSelected: (HistoryFilter) -> Unit,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 검색은 향후 구현 예정 (placeholder)
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search history...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onClearHistory) {
                Text("🗑️")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistoryFilter.entries.forEach { filter ->
                FilterChip(
                    selected = uiState.selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(filter.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.items) { result ->
                HistoryItem(result)
            }
        }
    }
}

@Composable
private fun HistoryItem(result: QRResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column {
                Text(text = result.title, style = MaterialTheme.typography.titleMedium)
                Text(text = "${result.scannedAt} • ${result.source.name}")
            }
        }
    }
}

@Preview
@Composable
private fun HistoryScreenPreview() {
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
