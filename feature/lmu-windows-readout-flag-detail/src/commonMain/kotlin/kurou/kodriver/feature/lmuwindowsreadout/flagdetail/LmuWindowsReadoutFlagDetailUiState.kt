package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.RedFlagVoiceType

internal data class LmuWindowsReadoutFlagDetailUiState(
    val enabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    val redFlagVoiceType: RedFlagVoiceType = RedFlagVoiceType.SESSION_STOP,
)
