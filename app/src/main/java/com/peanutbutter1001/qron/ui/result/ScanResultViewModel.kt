package com.peanutbutter1001.qron.ui.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peanutbutter1001.qron.domain.model.QRResult
import com.peanutbutter1001.qron.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanResultViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<QRResult?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        // Activity에서 전달받은 ID 추출
        val resultId = savedStateHandle.get<Long>("QR_RESULT_ID") ?: -1L
        if (resultId != -1L) {
            loadResult(resultId)
        }
    }

    private fun loadResult(id: Long) {
        viewModelScope.launch {
            _uiState.value = historyRepository.getResultById(id)
        }
    }
}
