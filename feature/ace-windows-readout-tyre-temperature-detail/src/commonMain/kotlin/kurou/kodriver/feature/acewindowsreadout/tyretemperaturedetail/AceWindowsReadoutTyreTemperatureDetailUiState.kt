package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT

internal data class AceWindowsReadoutTyreTemperatureDetailUiState(
    val overheatWarningEnabled: Boolean = true,
    val highThresholdCelsius: Int = ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
)
