package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsUseCase

internal class Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel(
    observeGt7Ps5RemainingFuelLaps: ObserveGt7Ps5RemainingFuelLapsUseCase,
    private val saveGt7Ps5RemainingFuelLaps: SaveGt7Ps5RemainingFuelLapsUseCase,
) : ViewModel() {

    val uiState: StateFlow<Gt7Ps5ReadoutRemainingFuelLapsDetailUiState> =
        observeGt7Ps5RemainingFuelLaps()
            .map { Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(remainingFuelLaps = it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(),
            )

    fun onRemainingFuelLapsChanged(laps: Int) {
        viewModelScope.launch {
            saveGt7Ps5RemainingFuelLaps(laps)
        }
    }

    fun onResetRemainingFuelLaps() {
        onRemainingFuelLapsChanged(DEFAULT_REMAINING_FUEL_LAPS)
    }
}
