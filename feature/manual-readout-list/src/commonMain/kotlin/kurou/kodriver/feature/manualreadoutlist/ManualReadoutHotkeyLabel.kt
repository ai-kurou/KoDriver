package kurou.kodriver.feature.manualreadoutlist

import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers

/** 未割り当ては null、割り当て済みは修飾キーとキー名を空白なしで返す。 */
internal fun manualReadoutHotkeyLabel(
    modifiers: ManualReadoutHotkeyModifiers,
    virtualKeyCode: Int?,
): String? {
    if (virtualKeyCode == null) return null
    val prefix =
        when (modifiers) {
            ManualReadoutHotkeyModifiers.CTRL_ALT_SHIFT -> "Ctrl+Alt+Shift"
            ManualReadoutHotkeyModifiers.CTRL_ALT -> "Ctrl+Alt"
            ManualReadoutHotkeyModifiers.CTRL_SHIFT -> "Ctrl+Shift"
            ManualReadoutHotkeyModifiers.ALT_SHIFT -> "Alt+Shift"
        }
    val keyName =
        when (virtualKeyCode) {
            in 0x30..0x39, in 0x41..0x5A -> virtualKeyCode.toChar().toString()
            else -> virtualKeyCode.toString()
        }
    return "$prefix+$keyName"
}
