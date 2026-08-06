package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase

@Suppress("LongParameterList")
internal class LmuWindowsReadoutTyreTemperatureDetailViewModel(
    observeHighThreshold: ObserveLmuWindowsTyreTemperatureHighThresholdUseCase,
    observeEnabledStates: ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase,
    observeLowWarningPhases: ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase,
    observeVehicleClassHighThreshold: ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase,
    private val saveHighThreshold: SaveLmuWindowsTyreTemperatureHighThresholdUseCase,
    private val saveEnabledState: SaveLmuWindowsTyreTemperatureEnabledStateUseCase,
    private val saveLowWarningPhases: SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase,
    private val saveVehicleClassHighThreshold: SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutTyreTemperatureDetailUiState> =
        combine(
            observeHighThreshold(),
            observeEnabledStates(),
            observeLowWarningPhases(),
            observeVehicleClassHighThreshold(),
        ) { highThresholdCelsius, states, lowWarningPhases, vehicleClassHighThresholdCelsius ->
            LmuWindowsReadoutTyreTemperatureDetailUiState(
                highThresholdCelsius = highThresholdCelsius,
                overheatWarningEnabled = states.getValue(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning),
                lowWarningEnabled = states.getValue(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning),
                lowWarningPhases = lowWarningPhases,
                vehicleClassHighThresholdCelsius = vehicleClassHighThresholdCelsius,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutTyreTemperatureDetailUiState(),
        )

    fun onHighThresholdChanged(celsius: Int) {
        viewModelScope.launch { saveHighThreshold(celsius) }
    }

    fun onHighThresholdReset() {
        viewModelScope.launch { saveHighThreshold(LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT) }
    }

    fun onOverheatWarningEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, enabled)
        }
    }

    fun onPreviewClicked() {
        playSpeechEvent(SpeechEvent.TyreOverheat)
    }

    fun onLowWarningEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning, enabled)
        }
    }

    fun onLowWarningPreviewClicked() {
        playSpeechEvent(SpeechEvent.TyreCold)
    }

    fun onLowWarningPhaseToggled(phase: SessionPhase) {
        val currentPhases = uiState.value.lowWarningPhases
        val updatedPhases = if (phase in currentPhases) currentPhases - phase else currentPhases + phase
        viewModelScope.launch { saveLowWarningPhases(updatedPhases) }
    }

    fun onVehicleClassHighThresholdChanged(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Int,
    ) {
        viewModelScope.launch { saveVehicleClassHighThreshold(vehicleClass, celsius) }
    }

    fun onVehicleClassHighThresholdReset(vehicleClass: LmuWindowsVehicleClassData) {
        viewModelScope.launch {
            saveVehicleClassHighThreshold(
                vehicleClass,
                lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(vehicleClass),
            )
        }
    }
}
