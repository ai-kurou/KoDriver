package kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail

import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT

internal data class LmuWindowsAutoReadoutRemainingFuelDetailUiState(
    val thresholdPercentage: Int = LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
    val readoutText: String = LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val enabled: Boolean = true,
)
