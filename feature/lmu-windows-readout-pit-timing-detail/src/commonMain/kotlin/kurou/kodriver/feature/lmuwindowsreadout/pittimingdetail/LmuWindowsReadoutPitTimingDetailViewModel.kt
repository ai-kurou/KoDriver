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

    fun onTyreWearEnabledChanged(enabled: Boolean) {
        _uiState.update { it.copy(tyreWearEnabled = enabled) }
    }
}
