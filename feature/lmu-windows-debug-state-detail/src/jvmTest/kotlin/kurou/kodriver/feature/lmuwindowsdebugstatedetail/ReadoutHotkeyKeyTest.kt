package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.ui.input.key.Key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutHotkeyKeyTest {
    @Test
    fun `F1からF12と英数字をWindows VKと表示名へ変換する`() {
        val keys =
            listOf(
                Triple(Key.F1, 0x70, "F1"),
                Triple(Key.F2, 0x71, "F2"),
                Triple(Key.F3, 0x72, "F3"),
                Triple(Key.F4, 0x73, "F4"),
                Triple(Key.F5, 0x74, "F5"),
                Triple(Key.F6, 0x75, "F6"),
                Triple(Key.F7, 0x76, "F7"),
                Triple(Key.F8, 0x77, "F8"),
                Triple(Key.F9, 0x78, "F9"),
                Triple(Key.F10, 0x79, "F10"),
                Triple(Key.F11, 0x7A, "F11"),
                Triple(Key.F12, 0x7B, "F12"),
                Triple(Key.A, 0x41, "A"),
                Triple(Key.B, 0x42, "B"),
                Triple(Key.C, 0x43, "C"),
                Triple(Key.D, 0x44, "D"),
                Triple(Key.E, 0x45, "E"),
                Triple(Key.F, 0x46, "F"),
                Triple(Key.G, 0x47, "G"),
                Triple(Key.H, 0x48, "H"),
                Triple(Key.I, 0x49, "I"),
                Triple(Key.J, 0x4A, "J"),
                Triple(Key.K, 0x4B, "K"),
                Triple(Key.L, 0x4C, "L"),
                Triple(Key.M, 0x4D, "M"),
                Triple(Key.N, 0x4E, "N"),
                Triple(Key.O, 0x4F, "O"),
                Triple(Key.P, 0x50, "P"),
                Triple(Key.Q, 0x51, "Q"),
                Triple(Key.R, 0x52, "R"),
                Triple(Key.S, 0x53, "S"),
                Triple(Key.T, 0x54, "T"),
                Triple(Key.U, 0x55, "U"),
                Triple(Key.V, 0x56, "V"),
                Triple(Key.W, 0x57, "W"),
                Triple(Key.X, 0x58, "X"),
                Triple(Key.Y, 0x59, "Y"),
                Triple(Key.Z, 0x5A, "Z"),
                Triple(Key.Zero, 0x30, "0"),
                Triple(Key.One, 0x31, "1"),
                Triple(Key.Two, 0x32, "2"),
                Triple(Key.Three, 0x33, "3"),
                Triple(Key.Four, 0x34, "4"),
                Triple(Key.Five, 0x35, "5"),
                Triple(Key.Six, 0x36, "6"),
                Triple(Key.Seven, 0x37, "7"),
                Triple(Key.Eight, 0x38, "8"),
                Triple(Key.Nine, 0x39, "9"),
            )
        keys.forEach { (key, code, name) ->
            assertEquals(code, readoutHotkeyVirtualKeyCode(key))
            assertEquals("Ctrl + Alt + Shift + $name", readoutHotkeyDisplayName(code))
        }
    }

    @Test
    fun `修飾キーと対象外キーを無視し未知コードの表示は数値へフォールバックする`() {
        val ignoredKeys =
            listOf(
                Key.CtrlLeft,
                Key.CtrlRight,
                Key.AltLeft,
                Key.AltRight,
                Key.ShiftLeft,
                Key.ShiftRight,
                Key.Enter,
                Key.Spacebar,
            )
        ignoredKeys.forEach {
            assertNull(readoutHotkeyVirtualKeyCode(it))
        }
        assertEquals("Ctrl + Alt + Shift + 0", readoutHotkeyDisplayName(0))
    }
}
