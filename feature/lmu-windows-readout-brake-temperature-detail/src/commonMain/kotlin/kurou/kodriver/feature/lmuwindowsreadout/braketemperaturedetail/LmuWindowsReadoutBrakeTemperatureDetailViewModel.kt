package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBrakeTemperatureHighThresholdUseCase

internal class LmuWindowsReadoutBrakeTemperatureDetailViewModel(
    observeHighThresholdCelsius: ObserveLmuWindowsBrakeTemperatureHighThresholdUseCase,
    private val saveHighThresholdCelsius: SaveLmuWindowsBrakeTemperatureHighThresholdUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutBrakeTemperatureDetailUiState> =
        observeHighThresholdCelsius()
            .map { LmuWindowsReadoutBrakeTemperatureDetailUiState(highThresholdCelsius = it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                LmuWindowsReadoutBrakeTemperatureDetailUiState(),
            )

    fun onWarningChipClicked() {
        playSpeechEvent(SpeechEvent.BrakeOverheat)
    }

    fun onThresholdChanged(celsius: Int) {
        viewModelScope.launch { saveHighThresholdCelsius(celsius) }
    }

    fun onThresholdReset() {
        viewModelScope.launch {
            saveHighThresholdCelsius(LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)
        }
    }
}
