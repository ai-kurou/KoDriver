package kurou.kodriver.domain.model

/** 画面に表示する修飾キーの名前。 */
public fun ManualReadoutHotkeyModifiers.displayName(): String =
    when (this) {
        ManualReadoutHotkeyModifiers.CTRL_ALT -> "Ctrl + Alt"
        ManualReadoutHotkeyModifiers.CTRL_SHIFT -> "Ctrl + Shift"
        ManualReadoutHotkeyModifiers.ALT_SHIFT -> "Alt + Shift"
    }
