package kurou.kodriver.domain.model

/** 手動読み上げに割り当てられる数字・英字の仮想キーコードかを返す。 */
fun isManualReadoutHotkeyVirtualKeyCode(code: Int): Boolean = code in 0x30..0x39 || code in 0x41..0x5A
