package kurou.kodriver.feature.gt7ps5autoreadout.remainingfueldetail

import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT

internal data class Gt7Ps5AutoReadoutRemainingFuelDetailUiState(
    val thresholdPercentage: Int = GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
    val readoutText: String = GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val enabled: Boolean = true,
)
