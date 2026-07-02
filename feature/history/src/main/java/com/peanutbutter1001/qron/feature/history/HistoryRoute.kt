package com.peanutbutter1001.qron.feature.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
