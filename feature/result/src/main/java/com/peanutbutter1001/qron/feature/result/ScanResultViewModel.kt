package com.peanutbutter1001.qron.feature.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peanutbutter1001.qron.domain.model.QRResult
import com.peanutbutter1001.qron.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 결과 화면의 단일 UiState (AGENTS.md: sealed interface + 단일 StateFlow). */
sealed interface ResultUiState {
    data object Loading : ResultUiState
    data class Success(val qrResult: QRResult) : ResultUiState
    data object NotFound : ResultUiState
}

@HiltViewModel
class ScanResultViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState = _uiState.asStateFlow()

    /** 네비게이션 인자로 전달된 결과 ID를 로드한다. Route에서 1회 호출. */
    fun load(id: Long) {
        if (id < 0L) {
            _uiState.value = ResultUiState.NotFound
            return
        }
        viewModelScope.launch {
            val result = historyRepository.getResultById(id)
            _uiState.value = result?.let { ResultUiState.Success(it) } ?: ResultUiState.NotFound
        }
    }
}
