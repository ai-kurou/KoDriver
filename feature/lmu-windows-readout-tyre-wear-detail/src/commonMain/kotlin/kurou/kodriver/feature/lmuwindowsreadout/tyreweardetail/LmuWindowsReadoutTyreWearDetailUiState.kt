package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_DEFAULT_THRESHOLD_PERCENTAGE

internal data class LmuWindowsReadoutTyreWearDetailUiState(
    val thresholdPercentage: Int = LMU_WINDOWS_TYRE_WEAR_DEFAULT_THRESHOLD_PERCENTAGE,
)
