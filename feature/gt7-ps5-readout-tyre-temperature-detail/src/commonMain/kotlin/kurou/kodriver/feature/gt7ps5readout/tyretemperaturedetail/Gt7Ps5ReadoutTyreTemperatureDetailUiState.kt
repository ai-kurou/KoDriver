package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT

internal data class Gt7Ps5ReadoutTyreTemperatureDetailUiState(
    val overheatWarningEnabled: Boolean = true,
    val highThresholdCelsius: Int = GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
)
