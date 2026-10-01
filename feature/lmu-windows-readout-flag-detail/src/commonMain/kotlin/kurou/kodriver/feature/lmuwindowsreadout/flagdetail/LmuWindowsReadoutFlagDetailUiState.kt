package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kurou.kodriver.domain.model.ReadoutItemKey

internal data class LmuWindowsReadoutFlagDetailUiState(
    val enabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    /** フラッグごとのカスタム読み上げ文言。空白の場合は読み上げない。 */
    val flagTexts: Map<FlagReadoutItem, String> = emptyMap(),
    /** OS標準のTTSを利用できるか。利用できない場合はカスタム文言の入力を受け付けない。 */
    val isTextToSpeechAvailable: Boolean = false,
) {
    /** [item] のカスタム読み上げ文言。未設定なら [FlagReadoutItem.defaultText]。 */
    fun flagText(item: FlagReadoutItem): String = flagTexts[item] ?: item.defaultText

    /** [item] に読み上げ文言があるか。 */
    fun isCustomTextSelected(item: FlagReadoutItem): Boolean = flagText(item).isNotBlank()
}
