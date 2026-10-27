package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutHotkeyModifiersTest {
    @Test
    fun `全修飾キーのWindows値を相互変換でき不明値は既定値になる`() {
        assertEquals(ManualReadoutHotkeyModifiers.CTRL_ALT, MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT)
        assertEquals(listOf(3, 6, 5), ManualReadoutHotkeyModifiers.entries.map { it.value })
        ManualReadoutHotkeyModifiers.entries.forEach {
            assertEquals(it, ManualReadoutHotkeyModifiers.fromValue(it.value))
        }
        listOf(null, 0, 7, 8).forEach {
            assertEquals(MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT, ManualReadoutHotkeyModifiers.fromValue(it))
        }
        assertEquals(MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT, ManualReadoutSet("target").hotkeyModifiers)
    }
}
