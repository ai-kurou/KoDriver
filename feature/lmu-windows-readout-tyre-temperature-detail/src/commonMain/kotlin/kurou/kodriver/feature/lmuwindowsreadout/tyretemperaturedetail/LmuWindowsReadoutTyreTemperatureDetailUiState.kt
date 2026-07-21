package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.lmuWindowsTyreTemperatureLowWarningDefaultPhases

internal data class LmuWindowsReadoutTyreTemperatureDetailUiState(
    val highThresholdCelsius: Int = 95,
    val overheatWarningEnabled: Boolean = true,
    val lowWarningEnabled: Boolean = true,
    val lowWarningPhases: Set<SessionPhase> = lmuWindowsTyreTemperatureLowWarningDefaultPhases,
)
