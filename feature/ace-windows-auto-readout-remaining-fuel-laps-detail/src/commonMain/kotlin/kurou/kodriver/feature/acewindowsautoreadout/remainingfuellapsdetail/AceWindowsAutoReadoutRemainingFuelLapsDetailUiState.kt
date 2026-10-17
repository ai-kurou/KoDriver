package kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail

import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT

internal data class AceWindowsAutoReadoutRemainingFuelLapsDetailUiState(
    val remainingFuelLaps: Int = ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT,
    val readoutText: String = ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT,
    val emptyReadoutText: String = ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val enabled: Boolean = true,
)
