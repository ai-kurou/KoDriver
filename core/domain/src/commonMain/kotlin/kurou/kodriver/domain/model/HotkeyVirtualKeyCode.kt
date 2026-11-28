package kurou.kodriver.domain.model

/** グローバルホットキーに割り当てられる数字・英字の仮想キーコードかを返す。 */
public fun isHotkeyVirtualKeyCode(code: Int): Boolean = code in 0x30..0x39 || code in 0x41..0x5A
