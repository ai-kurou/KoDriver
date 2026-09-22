package kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail

import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT

internal data class AceWindowsReadoutRemainingFuelLapsDetailUiState(
    val remainingFuelLaps: Int = ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT,
)
