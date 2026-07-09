package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kurou.kodriver.domain.model.SessionPhase

data class LmuWindowsReadoutTyreTemperatureDetailUiState(
    val highThresholdCelsius: Int = 90,
    val overheatWarningEnabled: Boolean = true,
    val lowWarningEnabled: Boolean = true,
    val lowWarningPhases: Set<SessionPhase> = setOf(
        SessionPhase.GARAGE,
        SessionPhase.WARM_UP,
        SessionPhase.GRID_WALK,
        SessionPhase.FORMATION,
    ),
)
