package kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail

import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_SELECTED_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsTyreTemperatureLowWarningDefaultPhases
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.model.Celsius

internal data class LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
    val overheatReadoutText: String = LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
    val coldReadoutText: String = LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val overheatWarningEnabled: Boolean = true,
    val lowWarningEnabled: Boolean = true,
    val lowWarningPhases: Set<SessionPhase> = lmuWindowsTyreTemperatureLowWarningDefaultPhases,
    val vehicleClassHighThresholdCelsius: Map<LmuWindowsVehicleClassData, Int> = emptyMap(),
    val selectedVehicleClass: LmuWindowsVehicleClassData = LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_SELECTED_DEFAULT,
) {
    val selectedVehicleClassHighThresholdCelsius: Celsius
        get() =
            vehicleClassHighThresholdCelsius[selectedVehicleClass]?.let { Celsius(it) }
                ?: lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(selectedVehicleClass)
}
