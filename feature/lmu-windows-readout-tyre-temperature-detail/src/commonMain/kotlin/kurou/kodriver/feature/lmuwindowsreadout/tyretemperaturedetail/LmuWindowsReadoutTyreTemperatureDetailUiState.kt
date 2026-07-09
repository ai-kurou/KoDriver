package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

data class LmuWindowsReadoutTyreTemperatureDetailUiState(
    val highThresholdCelsius: Int = 90,
    val overheatWarningEnabled: Boolean = true,
)
