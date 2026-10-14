package kurou.kodriver.domain.model

/**
 * 手動読み上げの1セット。ホットキーは後続PRで追加予定。
 *
 * @property id セット識別子。
 * @property itemKeys 読み上げ項目のキー。
 * @property itemTexts 項目キーごとの文言テンプレート。未保存の項目には既定文言を使う。
 */
data class ManualReadoutSet(
    val id: String,
    val itemKeys: List<String> = emptyList(),
    val itemTexts: Map<String, String> = emptyMap(),
)
