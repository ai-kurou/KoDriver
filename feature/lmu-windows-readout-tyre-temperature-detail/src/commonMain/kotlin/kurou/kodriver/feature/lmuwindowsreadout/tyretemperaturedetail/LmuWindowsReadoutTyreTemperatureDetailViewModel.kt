package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class LmuWindowsReadoutTyreTemperatureDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LmuWindowsReadoutTyreTemperatureDetailUiState())
    val uiState: StateFlow<LmuWindowsReadoutTyreTemperatureDetailUiState> = _uiState.asStateFlow()

    fun onHighThresholdChanged(celsius: Int) {
        _uiState.update { it.copy(highThresholdCelsius = celsius) }
    }

    fun onHighThresholdReset() {
        _uiState.update { it.copy(highThresholdCelsius = 90) }
    }
}
