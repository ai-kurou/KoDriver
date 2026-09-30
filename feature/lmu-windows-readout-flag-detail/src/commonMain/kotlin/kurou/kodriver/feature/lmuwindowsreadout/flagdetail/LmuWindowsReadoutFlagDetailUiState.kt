package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.RedFlagVoiceType

internal data class LmuWindowsReadoutFlagDetailUiState(
    val enabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    val redFlagVoiceType: RedFlagVoiceType = RedFlagVoiceType.SESSION_STOP,
    /** フラッグごとのカスタム読み上げ文言。空文字（または未設定）なら収録済みWAVで読み上げる。 */
    val flagTexts: Map<FlagReadoutItem, String> = emptyMap(),
    /** OS標準のTTSを利用できるか。利用できない場合はカスタム文言の入力を受け付けない。 */
    val isTextToSpeechAvailable: Boolean = false,
) {
    /** [item] のカスタム読み上げ文言。未設定なら [READOUT_CUSTOM_TEXT_DEFAULT]（空文字）。 */
    fun flagText(item: FlagReadoutItem): String = flagTexts[item] ?: READOUT_CUSTOM_TEXT_DEFAULT
}
