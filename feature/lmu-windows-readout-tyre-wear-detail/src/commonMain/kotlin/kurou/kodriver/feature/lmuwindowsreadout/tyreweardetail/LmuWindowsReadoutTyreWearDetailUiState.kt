package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT

internal data class LmuWindowsReadoutTyreWearDetailUiState(
    val thresholdPercentage: Int = LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT,
)
