package kurou.kodriver.domain.model

/**
 * 手動読み上げの1セット。ホットキーの修飾キーをセットごとに選択できる。
 *
 * @property id セット識別子。
 * @property itemKeys 読み上げ項目のキー。
 * @property itemTexts 項目キーごとの文言テンプレート。未保存の項目には既定文言を使う。
 * @property hotkeyModifiers ホットキーの修飾キー。
 * @property hotkeyVirtualKeyCode ホットキーの仮想キーコード。null は割り当てなし。
 * @property name 表示専用のセット名。空文字は名前なし。
 * @property number 作成時に採番する固定の表示番号。0は未採番で、その場合は一覧上の位置を使う。
 */
data class ManualReadoutSet(
    val id: String,
    val itemKeys: List<String> = emptyList(),
    val itemTexts: Map<String, String> = emptyMap(),
    val hotkeyVirtualKeyCode: Int? = null,
    val hotkeyModifiers: ManualReadoutHotkeyModifiers = MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT,
    val name: String = "",
    val number: Int = 0,
)
