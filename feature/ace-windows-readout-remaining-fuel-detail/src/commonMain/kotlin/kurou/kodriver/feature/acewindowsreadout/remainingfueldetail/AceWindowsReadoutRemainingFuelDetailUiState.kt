package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT

internal data class AceWindowsReadoutRemainingFuelDetailUiState(
    val thresholdPercentage: Int = ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
)
