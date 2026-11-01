package kurou.kodriver.feature.otheroverlaybackgroundopacitydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.PreviewOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.SaveOverlayBackgroundOpacityUseCase

internal class OtherOverlayBackgroundOpacityDetailViewModel(
    observeOverlayBackgroundOpacity: ObserveOverlayBackgroundOpacityUseCase,
    private val previewOverlayBackgroundOpacity: PreviewOverlayBackgroundOpacityUseCase,
    private val saveOverlayBackgroundOpacity: SaveOverlayBackgroundOpacityUseCase,
) : ViewModel() {
    private var selectionRequest = 0

    val uiState: StateFlow<OtherOverlayBackgroundOpacityDetailUiState> =
        OtherOverlayBackgroundOpacityDetailFlow(
            observedOverlayBackgroundOpacity = observeOverlayBackgroundOpacity(),
        ).uiState
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                OtherOverlayBackgroundOpacityDetailUiState(),
            )

    fun onOpacityPreview(opacity: Int) {
        selectionRequest++
        previewOverlayBackgroundOpacity(opacity)
    }

    fun onOpacityChanged(opacity: Int) {
        val request = ++selectionRequest
        viewModelScope.launch {
            try {
                saveOverlayBackgroundOpacity(opacity)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                captureOtherOverlayBackgroundOpacityDetailError(e)
                // 保存に失敗しても未保存の透明度を表示し続けず、保存済みの設定へ戻す。
            } finally {
                // 保存中に操作し直された場合は、後続の操作のプレビューを解除しない。
                if (selectionRequest == request) {
                    previewOverlayBackgroundOpacity(null)
                }
            }
        }
    }

    /** ペインが破棄されたときに、未保存のプレビューを解除して保存値へ戻す。 */
    fun onPreviewStopped() {
        selectionRequest++
        previewOverlayBackgroundOpacity(null)
    }

    override fun onCleared() {
        previewOverlayBackgroundOpacity(null)
        super.onCleared()
    }
}
