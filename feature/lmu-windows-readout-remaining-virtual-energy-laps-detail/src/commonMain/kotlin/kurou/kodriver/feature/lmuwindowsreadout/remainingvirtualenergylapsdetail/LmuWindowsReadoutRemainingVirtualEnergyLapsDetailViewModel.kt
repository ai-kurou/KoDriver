package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergylapsdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRemainingVirtualEnergyLapsUseCase

internal class LmuWindowsReadoutRemainingVirtualEnergyLapsDetailViewModel(
    observeLmuWindowsRemainingVirtualEnergyLaps: ObserveLmuWindowsRemainingVirtualEnergyLapsUseCase,
    private val saveLmuWindowsRemainingVirtualEnergyLaps: SaveLmuWindowsRemainingVirtualEnergyLapsUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutRemainingVirtualEnergyLapsDetailUiState> =
        observeLmuWindowsRemainingVirtualEnergyLaps()
            .map { LmuWindowsReadoutRemainingVirtualEnergyLapsDetailUiState(remainingVirtualEnergyLaps = it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                LmuWindowsReadoutRemainingVirtualEnergyLapsDetailUiState(),
            )

    fun onRemainingVirtualEnergyLapsChanged(laps: Int) {
        viewModelScope.launch {
            saveLmuWindowsRemainingVirtualEnergyLaps(laps)
        }
    }

    fun onResetRemainingVirtualEnergyLaps() {
        onRemainingVirtualEnergyLapsChanged(DEFAULT_REMAINING_VIRTUAL_ENERGY_LAPS)
    }
}
