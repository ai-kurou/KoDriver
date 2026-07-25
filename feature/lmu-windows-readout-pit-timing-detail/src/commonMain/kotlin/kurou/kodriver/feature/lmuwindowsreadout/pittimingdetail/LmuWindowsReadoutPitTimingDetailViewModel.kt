package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class LmuWindowsReadoutPitTimingDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LmuWindowsReadoutPitTimingDetailUiState())
    val uiState: StateFlow<LmuWindowsReadoutPitTimingDetailUiState> = _uiState.asStateFlow()

    fun onVirtualEnergyEnabledChanged(enabled: Boolean) {
        _uiState.update { it.copy(virtualEnergyEnabled = enabled) }
    }

    fun onVirtualEnergyLapsChanged(laps: Int) {
        _uiState.update { it.copy(virtualEnergyLaps = laps) }
    }

    fun onTyreWearLapsChanged(laps: Int) {
        _uiState.update { it.copy(tyreWearLaps = laps) }
    }

    companion object {
        const val DEFAULT_LAPS = 3
    }
}
