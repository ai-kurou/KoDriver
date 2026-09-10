package kurou.kodriver.feature.lmuwindowsreadout.raindetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRainEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRainEnabledStateUseCase

internal class LmuWindowsReadoutRainDetailViewModel(
    observeRainEnabledStates: ObserveLmuWindowsRainEnabledStatesUseCase,
    private val saveRainEnabledState: SaveLmuWindowsRainEnabledStateUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutRainDetailUiState> =
        observeRainEnabledStates()
            .map { states ->
                LmuWindowsReadoutRainDetailUiState(
                    startReadoutEnabled = states.getValue(ReadoutItemKey.LmuWindows.Rain.Start),
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                LmuWindowsReadoutRainDetailUiState(),
            )

    fun onStartReadoutEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { saveRainEnabledState(ReadoutItemKey.LmuWindows.Rain.Start, enabled) }
    }
}
