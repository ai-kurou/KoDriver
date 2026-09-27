package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_SELECTED_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData

internal data class LmuWindowsReadoutBrakeTemperatureDetailUiState(
    val vehicleClassHighThresholdCelsius: Map<LmuWindowsVehicleClassData, Int> = emptyMap(),
    val selectedVehicleClass: LmuWindowsVehicleClassData = LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_SELECTED_DEFAULT,
)
