package com.peanutbutter1001.qron.feature.result

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peanutbutter1001.qron.core.vision.QRImageCache
import com.peanutbutter1001.qron.domain.model.QRResult
import com.peanutbutter1001.qron.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 결과 화면의 단일 UiState (AGENTS.md: sealed interface + 단일 StateFlow). */
sealed interface ResultUiState {
    data object Loading : ResultUiState
    data class Success(
        val qrResult: QRResult,
        val croppedBitmap: Bitmap? = null   // 스캔 직후에만 존재. History 재진입 시 null
    ) : ResultUiState
    data object NotFound : ResultUiState
}

@HiltViewModel
class ScanResultViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val qrImageCache: QRImageCache
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    /** 네비게이션 인자로 전달된 결과 ID를 로드한다. Route에서 1회 호출. */
    fun load(id: Long) {
        if (id < 0L) {
            _uiState.value = ResultUiState.NotFound
            return
        }
        viewModelScope.launch {
            val result = historyRepository.getResultById(id)
            _uiState.value = result?.let {
                ResultUiState.Success(
                    qrResult = it,
                    croppedBitmap = qrImageCache.get(id)    // 캐시에 없으면 null (History 진입 등)
                )
            } ?: ResultUiState.NotFound
        }
    }

    /** 결과 화면이 닫힐 때 호출. 캐시에서 Bitmap을 제거하고 메모리를 해제한다. */
    fun evictImage(id: Long) {
        qrImageCache.evict(id)
    }

    /** Snackbar 메시지 요청. StringRes ID를 emit해 UI 계층에서 getString으로 변환한다. */
    fun showSnackbar(@StringRes resId: Int) {
        _snackbarMessage.tryEmit(resId)
    }
}
