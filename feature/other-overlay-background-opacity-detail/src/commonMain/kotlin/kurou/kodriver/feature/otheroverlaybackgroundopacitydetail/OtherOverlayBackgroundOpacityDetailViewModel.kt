package kurou.kodriver.feature.otheroverlaybackgroundopacitydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.SaveOverlayBackgroundOpacityUseCase

internal class OtherOverlayBackgroundOpacityDetailViewModel(
    observeOverlayBackgroundOpacity: ObserveOverlayBackgroundOpacityUseCase,
    private val saveOverlayBackgroundOpacity: SaveOverlayBackgroundOpacityUseCase,
) : ViewModel() {
    val uiState: StateFlow<OtherOverlayBackgroundOpacityDetailUiState> =
        observeOverlayBackgroundOpacity()
            .map { opacity -> OtherOverlayBackgroundOpacityDetailUiState(opacity = opacity) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                OtherOverlayBackgroundOpacityDetailUiState(),
            )

    fun onOpacityChanged(opacity: Int) {
        viewModelScope.launch { saveOverlayBackgroundOpacity(opacity) }
    }
}
