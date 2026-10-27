package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.ui.input.key.Key
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutHotkeyKeyTest {
    @Test
    fun `数字と英字の対応表と表示名を検証する`() {
        val keys =
            listOf(
                Key.Zero,
                Key.One,
                Key.Two,
                Key.Three,
                Key.Four,
                Key.Five,
                Key.Six,
                Key.Seven,
                Key.Eight,
                Key.Nine,
                Key.A,
                Key.B,
                Key.C,
                Key.D,
                Key.E,
                Key.F,
                Key.G,
                Key.H,
                Key.I,
                Key.J,
                Key.K,
                Key.L,
                Key.M,
                Key.N,
                Key.O,
                Key.P,
                Key.Q,
                Key.R,
                Key.S,
                Key.T,
                Key.U,
                Key.V,
                Key.W,
                Key.X,
                Key.Y,
                Key.Z,
            )
        val codes = (0x30..0x39).toList() + (0x41..0x5A).toList()
        keys.zip(codes).forEach { (key, code) ->
            assertEquals(code, manualReadoutHotkeyVirtualKeyCode(key))
            assertEquals("${code.toChar()}", manualReadoutHotkeyDisplayName(code))
        }
        assertEquals(
            listOf("Ctrl + Alt", "Ctrl + Shift", "Alt + Shift"),
            ManualReadoutHotkeyModifiers.entries.map { it.displayName() },
        )
        assertEquals(null, manualReadoutHotkeyVirtualKeyCode(Key.F1))
        assertEquals("112", manualReadoutHotkeyDisplayName(0x70))
    }
}
