package kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail

import kurou.kodriver.domain.model.LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT

internal data class LmuWindowsAutoReadoutMyBestLapDetailUiState(
    val readoutText: String = LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val enabled: Boolean = true,
)
