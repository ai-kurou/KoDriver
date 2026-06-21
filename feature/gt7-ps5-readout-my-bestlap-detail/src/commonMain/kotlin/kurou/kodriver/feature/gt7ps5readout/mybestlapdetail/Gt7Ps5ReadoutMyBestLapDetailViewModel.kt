package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase

private const val SIMULATOR_ID = "gt7_ps5"

internal class Gt7Ps5ReadoutMyBestLapDetailViewModel(
    observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    private val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
) : ViewModel() {

    val uiState: StateFlow<Gt7Ps5ReadoutMyBestLapDetailUiState> =
        observeReadoutEnabledStates(SIMULATOR_ID)
            .map { states ->
                Gt7Ps5ReadoutMyBestLapDetailUiState(
                    enabled = states[ReadoutItemKey.BEST_LAP] ?: true,
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                Gt7Ps5ReadoutMyBestLapDetailUiState(),
            )

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveReadoutEnabledState(SIMULATOR_ID, ReadoutItemKey.BEST_LAP, enabled)
        }
    }
}
