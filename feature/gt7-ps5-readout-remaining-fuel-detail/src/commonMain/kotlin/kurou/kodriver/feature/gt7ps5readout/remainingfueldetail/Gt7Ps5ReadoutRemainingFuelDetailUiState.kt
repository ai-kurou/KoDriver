package kurou.kodriver.feature.gt7ps5readout.remainingfueldetail

import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT

internal data class Gt7Ps5ReadoutRemainingFuelDetailUiState(
    val thresholdPercentage: Int = GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
)
