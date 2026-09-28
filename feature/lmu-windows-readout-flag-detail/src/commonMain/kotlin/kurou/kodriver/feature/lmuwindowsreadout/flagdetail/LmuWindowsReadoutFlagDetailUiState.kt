package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.RedFlagVoiceType

internal data class LmuWindowsReadoutFlagDetailUiState(
    val enabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    val redFlagVoiceType: RedFlagVoiceType = RedFlagVoiceType.SESSION_STOP,
    /** イエローフラッグのカスタム読み上げ文言。空文字なら収録済みWAVで読み上げる。 */
    val sectorYellowFlagText: String = READOUT_CUSTOM_TEXT_DEFAULT,
    /** OS標準のTTSを利用できるか。利用できない場合はカスタム文言の入力を受け付けない。 */
    val isTextToSpeechAvailable: Boolean = false,
)
