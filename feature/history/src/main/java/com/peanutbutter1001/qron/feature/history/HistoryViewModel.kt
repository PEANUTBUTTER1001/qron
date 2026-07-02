package com.peanutbutter1001.qron.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peanutbutter1001.qron.domain.model.QRResult
import com.peanutbutter1001.qron.domain.model.QRType
import com.peanutbutter1001.qron.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 기록 목록 필터. 라벨과 매칭 규칙을 함께 가진다. */
enum class HistoryFilter(val label: String) {
    ALL("전체"),
    LINK("링크"),
    PAYMENT("결제/쿠폰");

    fun matches(result: QRResult): Boolean = when (this) {
        ALL -> true
        LINK -> result.type == QRType.URL
        PAYMENT -> result.type == QRType.PAYMENT
    }
}

/** 기록 화면의 단일 UiState. */
data class HistoryUiState(
    val items: List<QRResult> = emptyList(),
    val selectedFilter: HistoryFilter = HistoryFilter.ALL
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(HistoryFilter.ALL)

    val uiState: StateFlow<HistoryUiState> =
        combine(historyRepository.getAllHistory(), selectedFilter) { history, filter ->
            HistoryUiState(
                items = history.filter { filter.matches(it) },
                selectedFilter = filter
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState()
        )

    fun onFilterSelected(filter: HistoryFilter) {
        selectedFilter.value = filter
    }

    fun clearHistory() {
        viewModelScope.launch { historyRepository.clearHistory() }
    }
}
