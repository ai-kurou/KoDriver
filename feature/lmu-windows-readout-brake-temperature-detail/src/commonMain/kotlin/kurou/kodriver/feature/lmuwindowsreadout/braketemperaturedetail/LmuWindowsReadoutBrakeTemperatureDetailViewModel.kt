package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase

internal data class BrakeTemperatureUseCases(
    val observeVehicleClassHighThreshold: ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase,
    val observeVehicleClassSelection: ObserveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase,
    val saveVehicleClassHighThreshold: SaveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase,
    val saveVehicleClassSelection: SaveLmuWindowsVehicleClassBrakeTemperatureSelectionUseCase,
)

internal class LmuWindowsReadoutBrakeTemperatureDetailViewModel(
    private val brakeTemperatureUseCases: BrakeTemperatureUseCases,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutBrakeTemperatureDetailUiState> =
        combine(
            brakeTemperatureUseCases.observeVehicleClassHighThreshold(),
            brakeTemperatureUseCases.observeVehicleClassSelection(),
        ) { vehicleClassHighThresholdCelsius, selectedVehicleClass ->
            LmuWindowsReadoutBrakeTemperatureDetailUiState(
                vehicleClassHighThresholdCelsius = vehicleClassHighThresholdCelsius,
                selectedVehicleClass = selectedVehicleClass,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutBrakeTemperatureDetailUiState(),
        )

    fun onWarningChipClicked() {
        playSpeechEvent(SpeechEvent.BrakeOverheat)
    }

    fun onVehicleClassSelected(vehicleClass: LmuWindowsVehicleClassData) {
        viewModelScope.launch { brakeTemperatureUseCases.saveVehicleClassSelection(vehicleClass) }
    }

    fun onVehicleClassHighThresholdChanged(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Int,
    ) {
        viewModelScope.launch {
            brakeTemperatureUseCases.saveVehicleClassHighThreshold(vehicleClass, celsius)
        }
    }

    fun onVehicleClassHighThresholdReset(vehicleClass: LmuWindowsVehicleClassData) {
        viewModelScope.launch {
            brakeTemperatureUseCases.saveVehicleClassHighThreshold(
                vehicleClass,
                lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(vehicleClass),
            )
        }
    }
}
