package com.peanutbutter1001.qron

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.peanutbutter1001.qron.feature.history.HistoryScreen
import com.peanutbutter1001.qron.feature.scanner.ScannerScreen
import com.peanutbutter1001.qron.core.designsystem.theme.QRonTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QRonTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("📷") },
                    label = { Text("스캐너") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("🕒") },
                    label = { Text("기록") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> ScannerScreen(
                    onExternalScanSelected = { /* 카메라 프리뷰 활성화 */ },
                    onInternalScanSelected = { /* 접근성 서비스 등 안내 */ }
                )
                1 -> {
                    val historyViewModel: com.peanutbutter1001.qron.feature.history.HistoryViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                    val historyList by historyViewModel.historyList.collectAsState()
                    HistoryScreen(
                        historyList = historyList,
                        onFilterSelected = { /* 필터 처리 */ },
                        onClearHistory = { historyViewModel.clearHistory() }
                    )
                }
            }
        }
    }
}