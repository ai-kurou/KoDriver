package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kurou.kodriver.core.model.LmuWindowsVehicleClassData
import kurou.kodriver.core.model.SessionPhase
import kurou.kodriver.core.model.lmuWindowsTyreTemperatureLowWarningDefaultPhases
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_SELECTED_DEFAULT

internal data class LmuWindowsReadoutTyreTemperatureDetailUiState(
    val overheatWarningEnabled: Boolean = true,
    val lowWarningEnabled: Boolean = true,
    val lowWarningPhases: Set<SessionPhase> = lmuWindowsTyreTemperatureLowWarningDefaultPhases,
    val vehicleClassHighThresholdCelsius: Map<LmuWindowsVehicleClassData, Int> = emptyMap(),
    val selectedVehicleClass: LmuWindowsVehicleClassData = LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_SELECTED_DEFAULT,
)
