package kurou.kodriver.feature.otheroverlaytextsizedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.usecase.ObserveOverlayTextSizeUseCase
import kurou.kodriver.domain.usecase.SaveOverlayTextSizeUseCase

/**
 * OtherOverlayTextSizeDetail 画面の状態管理とユーザー操作を扱う ViewModel。
 */
class OtherOverlayTextSizeDetailViewModel internal constructor(
    observeOverlayTextSize: ObserveOverlayTextSizeUseCase,
    private val saveOverlayTextSize: SaveOverlayTextSizeUseCase,
) : ViewModel() {
    private val pendingOverlayTextSize = MutableStateFlow<OverlayTextSize?>(null)

    internal val uiState =
        combine(observeOverlayTextSize(), pendingOverlayTextSize) { saved, pending ->
            OtherOverlayTextSizeDetailUiState(
                selectedOverlayTextSize = saved,
                pendingOverlayTextSize = pending ?: saved,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherOverlayTextSizeDetailUiState())

    internal fun onPendingOverlayTextSizeSelected(overlayTextSize: OverlayTextSize) {
        pendingOverlayTextSize.update { overlayTextSize }
    }

    internal fun onConfirm() {
        val overlayTextSize = pendingOverlayTextSize.value ?: return
        viewModelScope.launch { saveOverlayTextSize(overlayTextSize) }
        pendingOverlayTextSize.update { null }
    }

    internal fun onDismiss() {
        pendingOverlayTextSize.update { null }
    }
}
