package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_SELECTED_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData

internal data class LmuWindowsReadoutBrakeTemperatureDetailUiState(
    val vehicleClassHighThresholdCelsius: Map<LmuWindowsVehicleClassData, Int> = emptyMap(),
    val selectedVehicleClass: LmuWindowsVehicleClassData = LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_SELECTED_DEFAULT,
    val readoutText: String = LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val enabled: Boolean = true,
)
