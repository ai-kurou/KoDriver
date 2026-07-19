package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

import kurou.kodriver.domain.model.MyBestLapVoiceType

internal data class LmuWindowsReadoutMyBestLapDetailUiState(
    val voiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
)
