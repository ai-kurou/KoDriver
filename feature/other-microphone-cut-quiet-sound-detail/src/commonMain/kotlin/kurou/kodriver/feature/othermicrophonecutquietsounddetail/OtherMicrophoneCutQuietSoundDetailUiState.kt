package kurou.kodriver.feature.othermicrophonecutquietsounddetail

import kurou.kodriver.domain.microphone.model.MICROPHONE_CUT_QUIET_SOUND_IN_COMMAND_DEFAULT
import kurou.kodriver.domain.microphone.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT

/**
 * OtherMicrophoneCutQuietSoundDetail 画面の表示状態。
 */
public data class OtherMicrophoneCutQuietSoundDetailUiState(
    val level: Int = MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT,
    val cutInCommand: Boolean = MICROPHONE_CUT_QUIET_SOUND_IN_COMMAND_DEFAULT,
)
