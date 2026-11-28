package kurou.kodriver.domain.speechrecognition.model

import kurou.kodriver.domain.model.MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers

/** ウェイクワードの代わりに押すホットキーの割り当て。[virtualKeyCode] が null なら未割り当て。 */
public data class WakeWordHotkey(
    val virtualKeyCode: Int? = null,
    val modifiers: ManualReadoutHotkeyModifiers = MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT,
)
