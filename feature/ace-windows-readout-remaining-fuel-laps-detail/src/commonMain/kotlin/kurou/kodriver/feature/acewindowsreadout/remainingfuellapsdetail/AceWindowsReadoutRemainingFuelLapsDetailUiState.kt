package kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail

import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT

internal data class AceWindowsReadoutRemainingFuelLapsDetailUiState(
    val remainingFuelLaps: Int = ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT,
    val readoutText: String = ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT,
    val emptyReadoutText: String = ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val enabled: Boolean = true,
)
