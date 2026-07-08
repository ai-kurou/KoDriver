package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kurou.kodriver.domain.model.SessionPhase

data class LmuWindowsReadoutTyreTemperatureDetailUiState(
    val highThresholdCelsius: Int = 90,
    val gamePhase: SessionPhase = SessionPhase.UNKNOWN,
)
