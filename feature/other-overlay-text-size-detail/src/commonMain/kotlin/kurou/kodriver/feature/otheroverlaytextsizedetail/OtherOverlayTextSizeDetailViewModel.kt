package kurou.kodriver.feature.otheroverlaytextsizedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.usecase.ObserveOverlayTextSizeUseCase
import kurou.kodriver.domain.usecase.PreviewOverlayTextSizeUseCase
import kurou.kodriver.domain.usecase.SaveOverlayTextSizeUseCase

/**
 * OtherOverlayTextSizeDetail 画面の状態管理とユーザー操作を扱う ViewModel。
 */
class OtherOverlayTextSizeDetailViewModel internal constructor(
    observeOverlayTextSize: ObserveOverlayTextSizeUseCase,
    private val previewOverlayTextSize: PreviewOverlayTextSizeUseCase,
    private val saveOverlayTextSize: SaveOverlayTextSizeUseCase,
) : ViewModel() {
    private var selectionRequest = 0
    private var confirmingRequest: Int? = null

    private val pendingOverlayTextSize = MutableStateFlow<OverlayTextSize?>(null)

    internal val uiState =
        OtherOverlayTextSizeDetailFlow(
            observedOverlayTextSize = observeOverlayTextSize(),
            pendingOverlayTextSize = pendingOverlayTextSize,
        ).uiState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherOverlayTextSizeDetailUiState())

    internal fun onPendingOverlayTextSizeSelected(overlayTextSize: OverlayTextSize) {
        selectionRequest++
        pendingOverlayTextSize.update { overlayTextSize }
        previewOverlayTextSize(overlayTextSize)
    }

    internal fun onConfirm() {
        val overlayTextSize = pendingOverlayTextSize.value ?: return
        val request = ++selectionRequest
        confirmingRequest = request
        viewModelScope.launch {
            try {
                saveOverlayTextSize(overlayTextSize)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // 保存に失敗しても未保存のサイズを表示し続けず、保存済みの設定へ戻す。
            } finally {
                if (confirmingRequest == request) confirmingRequest = null
                // 保存中に選び直された場合は、後続の選択のプレビューを解除しない。
                if (selectionRequest == request) {
                    previewOverlayTextSize(null)
                    pendingOverlayTextSize.update { null }
                }
            }
        }
    }

    override fun onCleared() {
        previewOverlayTextSize(null)
        super.onCleared()
    }

    /**
     * ダイアログが破棄されたときに、未保存のプレビューを解除して保存値へ戻す。
     * 確定した選択が保存中の間は、保存完了時に解除するためプレビューと選択値を維持する。
     * 保存中に選び直された場合は、未保存のプレビューと選択値を解除する。
     */
    internal fun onPreviewStopped() {
        if (confirmingRequest != null && confirmingRequest == selectionRequest) return
        onDismiss()
    }

    internal fun onDismiss() {
        selectionRequest++
        previewOverlayTextSize(null)
        pendingOverlayTextSize.update { null }
    }
}
