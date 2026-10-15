package kurou.kodriver.domain.model

/**
 * 手動読み上げの1セット。ホットキーの修飾キーは Ctrl+Alt+Shift 固定。
 *
 * @property id セット識別子。
 * @property itemKeys 読み上げ項目のキー。
 * @property itemTexts 項目キーごとの文言テンプレート。未保存の項目には既定文言を使う。
 * @property hotkeyVirtualKeyCode ホットキーの仮想キーコード。null は割り当てなし。
 */
data class ManualReadoutSet(
    val id: String,
    val itemKeys: List<String> = emptyList(),
    val itemTexts: Map<String, String> = emptyMap(),
    val hotkeyVirtualKeyCode: Int? = null,
)
