package kurou.kodriver.feature.acewindowsreadout.mybestlapdetail

import kurou.kodriver.domain.model.MyBestLapVoiceType

internal data class AceWindowsReadoutMyBestLapDetailUiState(
    val voiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
)
