package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT

internal data class Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
    val remainingFuelLaps: Int = GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT,
)
