package kurou.kodriver.feature.acewindowsreadout.mybestlapdetail

import kurou.kodriver.domain.model.ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT

internal data class AceWindowsReadoutMyBestLapDetailUiState(
    val readoutText: String = ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val enabled: Boolean = true,
)
