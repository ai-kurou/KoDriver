package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.lmuWindowsTyreTemperatureLowWarningDefaultPhases

internal data class LmuWindowsReadoutTyreTemperatureDetailUiState(
    val highThresholdCelsius: Int = LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
    val overheatWarningEnabled: Boolean = true,
    val lowWarningEnabled: Boolean = true,
    val lowWarningPhases: Set<SessionPhase> = lmuWindowsTyreTemperatureLowWarningDefaultPhases,
)
