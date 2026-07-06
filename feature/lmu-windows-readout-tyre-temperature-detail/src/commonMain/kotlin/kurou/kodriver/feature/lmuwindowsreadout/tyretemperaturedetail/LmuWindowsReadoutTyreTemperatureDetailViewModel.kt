package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureHighThresholdUseCase

internal class LmuWindowsReadoutTyreTemperatureDetailViewModel(
    observeHighThreshold: ObserveLmuWindowsTyreTemperatureHighThresholdUseCase,
    private val saveHighThreshold: SaveLmuWindowsTyreTemperatureHighThresholdUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutTyreTemperatureDetailUiState> =
        observeHighThreshold()
            .map { LmuWindowsReadoutTyreTemperatureDetailUiState(highThresholdCelsius = it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                LmuWindowsReadoutTyreTemperatureDetailUiState(),
            )

    fun onHighThresholdChanged(celsius: Int) {
        viewModelScope.launch { saveHighThreshold(celsius) }
    }

    fun onHighThresholdReset() {
        viewModelScope.launch { saveHighThreshold(DEFAULT_HIGH_THRESHOLD_CELSIUS) }
    }

    companion object {
        const val DEFAULT_HIGH_THRESHOLD_CELSIUS = 90
    }
}
