package kurou.kodriver.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.usecase.ObserveLowVolumeWarningUseCase

class LowVolumeWarningViewModel(
    observeLowVolumeWarning: ObserveLowVolumeWarningUseCase,
) : ViewModel() {
    private val dismissed = MutableStateFlow(false)

    /** 閉じた後は、警告が解消されるまで再表示しない。 */
    val uiState: StateFlow<LowVolumeWarningUiState> =
        LowVolumeWarningFlow(
            warning = observeLowVolumeWarning(),
            dismissed = dismissed,
            resetDismissal = { dismissed.update { false } },
        ).uiState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LowVolumeWarningUiState(),
        )

    fun dismiss() {
        if (!uiState.value.isVisible) return
        dismissed.update { true }
    }
}
