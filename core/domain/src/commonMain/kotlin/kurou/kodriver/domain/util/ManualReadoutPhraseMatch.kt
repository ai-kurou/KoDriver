package kurou.kodriver.domain.util

/** 音声認識の発話が一致した手動読み上げ項目のキー [itemKey] と、決め手になった語句 [phrase]（登録されている表記のまま）。 */
data class ManualReadoutPhraseMatch(
    val itemKey: String,
    val phrase: String,
)
