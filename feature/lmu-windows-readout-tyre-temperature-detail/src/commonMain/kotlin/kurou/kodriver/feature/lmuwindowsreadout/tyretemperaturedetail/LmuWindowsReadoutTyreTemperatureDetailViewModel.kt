package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase

internal data class TyreTemperatureUseCases(
    val observeEnabledStates: ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase,
    val observeLowWarningPhases: ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase,
    val observeVehicleClassHighThreshold: ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase,
    val observeVehicleClassSelection: ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase,
    val saveEnabledState: SaveLmuWindowsTyreTemperatureEnabledStateUseCase,
    val saveLowWarningPhases: SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase,
    val saveVehicleClassHighThreshold: SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase,
    val saveVehicleClassSelection: SaveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase,
)

internal class LmuWindowsReadoutTyreTemperatureDetailViewModel(
    private val tyreTemperatureUseCases: TyreTemperatureUseCases,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutTyreTemperatureDetailUiState> =
        combine(
            tyreTemperatureUseCases.observeEnabledStates(),
            tyreTemperatureUseCases.observeLowWarningPhases(),
            tyreTemperatureUseCases.observeVehicleClassHighThreshold(),
            tyreTemperatureUseCases.observeVehicleClassSelection(),
        ) { states, lowWarningPhases, vehicleClassHighThresholdCelsius, selectedVehicleClass ->
            LmuWindowsReadoutTyreTemperatureDetailUiState(
                overheatWarningEnabled = states.getValue(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning),
                lowWarningEnabled = states.getValue(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning),
                lowWarningPhases = lowWarningPhases,
                vehicleClassHighThresholdCelsius = vehicleClassHighThresholdCelsius.mapValues { it.value.value },
                selectedVehicleClass = selectedVehicleClass,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutTyreTemperatureDetailUiState(),
        )

    fun onOverheatWarningEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            tyreTemperatureUseCases.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, enabled)
        }
    }

    fun onPreviewClicked() {
        playSpeechEvent(SpeechEvent.TyreOverheat)
    }

    fun onLowWarningEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            tyreTemperatureUseCases.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning, enabled)
        }
    }

    fun onLowWarningPreviewClicked() {
        playSpeechEvent(SpeechEvent.TyreCold)
    }

    fun onLowWarningPhaseToggled(phase: SessionPhase) {
        val currentPhases = uiState.value.lowWarningPhases
        val updatedPhases = if (phase in currentPhases) currentPhases - phase else currentPhases + phase
        viewModelScope.launch { tyreTemperatureUseCases.saveLowWarningPhases(updatedPhases) }
    }

    fun onVehicleClassHighThresholdChanged(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Int,
    ) {
        viewModelScope.launch {
            tyreTemperatureUseCases.saveVehicleClassHighThreshold(vehicleClass, Celsius(celsius))
        }
    }

    fun onVehicleClassHighThresholdReset(vehicleClass: LmuWindowsVehicleClassData) {
        viewModelScope.launch {
            tyreTemperatureUseCases.saveVehicleClassHighThreshold(
                vehicleClass,
                lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(vehicleClass),
            )
        }
    }

    fun onVehicleClassSelected(vehicleClass: LmuWindowsVehicleClassData) {
        viewModelScope.launch { tyreTemperatureUseCases.saveVehicleClassSelection(vehicleClass) }
    }
}
