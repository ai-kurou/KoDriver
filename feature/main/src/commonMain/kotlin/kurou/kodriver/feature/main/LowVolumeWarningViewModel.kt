package kurou.kodriver.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.LowVolumeWarning
import kurou.kodriver.domain.usecase.DismissLowVolumeWarningUseCase
import kurou.kodriver.domain.usecase.ObserveLowVolumeWarningUseCase

class LowVolumeWarningViewModel(
    observeLowVolumeWarning: ObserveLowVolumeWarningUseCase,
    private val dismissLowVolumeWarning: DismissLowVolumeWarningUseCase,
) : ViewModel() {
    val uiState: StateFlow<LowVolumeWarningUiState> =
        observeLowVolumeWarning()
            .map { warning ->
                LowVolumeWarningUiState(warning?.deviceVolume, warning?.soundVolume)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = LowVolumeWarningUiState(),
            )

    fun dismiss() {
        val current = uiState.value
        if (!current.isVisible) return
        viewModelScope.launch {
            dismissLowVolumeWarning(LowVolumeWarning(current.deviceVolume, current.soundVolume))
        }
    }
}
