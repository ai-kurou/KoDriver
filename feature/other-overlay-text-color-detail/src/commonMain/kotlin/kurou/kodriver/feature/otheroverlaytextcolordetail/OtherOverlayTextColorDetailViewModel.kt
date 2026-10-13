package kurou.kodriver.feature.otheroverlaytextcolordetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveOverlayTextColorUseCase
import kurou.kodriver.domain.usecase.PreviewOverlayTextColorUseCase
import kurou.kodriver.domain.usecase.SaveOverlayTextColorUseCase

/** オーバーレイ文字色の保存値とダイアログ内の選択を管理する。 */
class OtherOverlayTextColorDetailViewModel internal constructor(
    observeOverlayTextColor: ObserveOverlayTextColorUseCase,
    private val previewOverlayTextColor: PreviewOverlayTextColorUseCase,
    private val saveOverlayTextColor: SaveOverlayTextColorUseCase,
) : ViewModel() {
    private var selectionRequest = 0
    private var confirmingRequest: Int? = null

    private val pendingOverlayTextColor = MutableStateFlow<Long?>(null)

    internal val uiState =
        combine(observeOverlayTextColor(), pendingOverlayTextColor) { observed, pending ->
            OtherOverlayTextColorDetailUiState(
                selectedOverlayTextColor = observed,
                pendingOverlayTextColor = pending ?: observed,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherOverlayTextColorDetailUiState())

    internal fun onPendingOverlayTextColorSelected(overlayTextColor: Long) {
        selectionRequest++
        pendingOverlayTextColor.update { overlayTextColor }
        previewOverlayTextColor(overlayTextColor)
    }

    internal fun onConfirm() {
        val overlayTextColor = pendingOverlayTextColor.value ?: return
        val request = ++selectionRequest
        confirmingRequest = request
        viewModelScope.launch {
            try {
                saveOverlayTextColor(overlayTextColor)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // 保存に失敗しても未保存の色を表示し続けず、保存済みの設定へ戻す。
            } finally {
                if (confirmingRequest == request) confirmingRequest = null
                // 保存中に選び直された場合は、後続の選択のプレビューを解除しない。
                if (selectionRequest == request) {
                    previewOverlayTextColor(null)
                    pendingOverlayTextColor.update { null }
                }
            }
        }
    }

    override fun onCleared() {
        previewOverlayTextColor(null)
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
        previewOverlayTextColor(null)
        pendingOverlayTextColor.update { null }
    }
}
