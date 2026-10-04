package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT

internal data class Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
    val remainingFuelLaps: Int = GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT,
    val readoutText: String = GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT,
    val emptyReadoutText: String = GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val enabled: Boolean = true,
)
