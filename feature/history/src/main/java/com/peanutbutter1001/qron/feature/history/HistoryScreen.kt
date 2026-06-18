package com.peanutbutter1001.qron.feature.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peanutbutter1001.qron.domain.model.QRResult

@Composable
fun HistoryScreen(
    historyList: List<QRResult>,
    onFilterSelected: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // Search bar placeholder
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search history...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onClearHistory) {
                Text("🗑️") // 휴지통 아이콘 (임시)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filter Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = true, onClick = { onFilterSelected("ALL") }, label = { Text("전체") })
            FilterChip(selected = false, onClick = { onFilterSelected("LINK") }, label = { Text("링크") })
            FilterChip(selected = false, onClick = { onFilterSelected("PAYMENT") }, label = { Text("결제/쿠폰") })
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // List
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(historyList) { result ->
                HistoryItem(result)
            }
        }
    }
}

@Composable
fun HistoryItem(result: QRResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column {
                Text(text = result.title, style = MaterialTheme.typography.titleMedium)
                Text(text = "${result.scannedAt} • ${result.source.name}")
            }
        }
    }
}
