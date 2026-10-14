package kurou.kodriver.domain.model

/**
 * 手動読み上げの1セット。文言・ホットキーは後続PRで追加予定。
 *
 * @property id セット識別子。
 * @property itemKeys 読み上げ項目のキー。
 */
data class ManualReadoutSet(
    val id: String,
    val itemKeys: List<String> = emptyList(),
)
