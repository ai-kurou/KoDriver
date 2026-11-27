package kurou.kodriver.feature.manualreadoutdetail

import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers

internal fun ManualReadoutHotkeyModifiers.displayName(): String =
    when (this) {
        ManualReadoutHotkeyModifiers.CTRL_ALT -> "Ctrl + Alt"
        ManualReadoutHotkeyModifiers.CTRL_SHIFT -> "Ctrl + Shift"
        ManualReadoutHotkeyModifiers.ALT_SHIFT -> "Alt + Shift"
    }
