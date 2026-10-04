package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT

internal data class LmuWindowsReadoutTyreWearDetailUiState(
    val thresholdPercentage: Int = LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT,
    val readoutText: String = LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val enabled: Boolean = true,
)
