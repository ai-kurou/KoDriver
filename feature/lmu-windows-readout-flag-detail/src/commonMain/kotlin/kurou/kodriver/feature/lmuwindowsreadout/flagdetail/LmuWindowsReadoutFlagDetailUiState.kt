package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_DEFAULT
import kurou.kodriver.domain.model.READOUT_RECORDED_VOICE_SELECTED_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.RedFlagVoiceType

internal data class LmuWindowsReadoutFlagDetailUiState(
    val enabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    val redFlagVoiceType: RedFlagVoiceType = RedFlagVoiceType.SESSION_STOP,
    /** フラッグごとのカスタム読み上げ文言（レッドフラッグは音声種別の違いによらず1つを共有する）。空文字（または未設定）なら収録済みWAVで読み上げる。 */
    val flagTexts: Map<FlagReadoutItem, String> = emptyMap(),
    /** フラッグごとに収録音声が明示的に選ばれているか。true の間は、文言が残っていても収録音声で読み上げる。 */
    val recordedVoiceSelected: Map<FlagReadoutItem, Boolean> = emptyMap(),
    /** OS標準のTTSを利用できるか。利用できない場合はカスタム文言の入力を受け付けない。 */
    val isTextToSpeechAvailable: Boolean = false,
) {
    /** [item] のカスタム読み上げ文言。未設定なら [READOUT_CUSTOM_TEXT_DEFAULT]（空文字）。 */
    fun flagText(item: FlagReadoutItem): String = flagTexts[item] ?: READOUT_CUSTOM_TEXT_DEFAULT

    /** [item] で収録音声が明示的に選ばれているか。未設定なら [READOUT_RECORDED_VOICE_SELECTED_DEFAULT]。 */
    fun isRecordedVoiceSelected(item: FlagReadoutItem): Boolean =
        recordedVoiceSelected[item] ?: READOUT_RECORDED_VOICE_SELECTED_DEFAULT

    /** [item] でカスタム文言が読み上げに使われるか。文言があり、かつ収録音声が明示的に選ばれていない場合。 */
    fun isCustomTextSelected(item: FlagReadoutItem): Boolean =
        flagText(item).isNotEmpty() && !isRecordedVoiceSelected(item)
}
