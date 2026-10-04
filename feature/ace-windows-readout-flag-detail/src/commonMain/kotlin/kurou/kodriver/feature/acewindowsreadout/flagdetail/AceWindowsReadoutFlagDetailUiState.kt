package kurou.kodriver.feature.acewindowsreadout.flagdetail

import kurou.kodriver.domain.model.ReadoutItemKey

internal data class AceWindowsReadoutFlagDetailUiState(
    val enabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
    /** フラッグごとの読み上げ文言。空白の場合は読み上げない。 */
    val flagTexts: Map<FlagReadoutItem, String> = emptyMap(),
    /** OS標準のTTSを利用できるか。利用できない場合は文言の入力を受け付けない。 */
    val isTextToSpeechAvailable: Boolean = false,
) {
    /** [item] の読み上げ文言。未設定なら [FlagReadoutItem.defaultText]。 */
    fun flagText(item: FlagReadoutItem): String = flagTexts[item] ?: item.defaultText.orEmpty()

    /** [item] に読み上げ文言があるか。 */
    fun hasReadoutText(item: FlagReadoutItem): Boolean = flagText(item).isNotBlank()
}
