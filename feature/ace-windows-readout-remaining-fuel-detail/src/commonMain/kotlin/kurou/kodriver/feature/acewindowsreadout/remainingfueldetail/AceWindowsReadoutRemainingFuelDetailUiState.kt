package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_DEFAULT_THRESHOLD_PERCENTAGE

internal data class AceWindowsReadoutRemainingFuelDetailUiState(
    val thresholdPercentage: Int = ACE_WINDOWS_REMAINING_FUEL_DEFAULT_THRESHOLD_PERCENTAGE,
)
