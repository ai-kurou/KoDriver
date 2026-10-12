package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.ui.input.key.Key

private data class ReadoutHotkeyKey(
    val key: Key,
    val virtualKeyCode: Int,
    val displayName: String,
)

private val readoutHotkeyKeys =
    listOf(
        ReadoutHotkeyKey(Key.F1, 0x70, "F1"),
        ReadoutHotkeyKey(Key.F2, 0x71, "F2"),
        ReadoutHotkeyKey(Key.F3, 0x72, "F3"),
        ReadoutHotkeyKey(Key.F4, 0x73, "F4"),
        ReadoutHotkeyKey(Key.F5, 0x74, "F5"),
        ReadoutHotkeyKey(Key.F6, 0x75, "F6"),
        ReadoutHotkeyKey(Key.F7, 0x76, "F7"),
        ReadoutHotkeyKey(Key.F8, 0x77, "F8"),
        ReadoutHotkeyKey(Key.F9, 0x78, "F9"),
        ReadoutHotkeyKey(Key.F10, 0x79, "F10"),
        ReadoutHotkeyKey(Key.F11, 0x7A, "F11"),
        ReadoutHotkeyKey(Key.F12, 0x7B, "F12"),
        ReadoutHotkeyKey(Key.A, 0x41, "A"),
        ReadoutHotkeyKey(Key.B, 0x42, "B"),
        ReadoutHotkeyKey(Key.C, 0x43, "C"),
        ReadoutHotkeyKey(Key.D, 0x44, "D"),
        ReadoutHotkeyKey(Key.E, 0x45, "E"),
        ReadoutHotkeyKey(Key.F, 0x46, "F"),
        ReadoutHotkeyKey(Key.G, 0x47, "G"),
        ReadoutHotkeyKey(Key.H, 0x48, "H"),
        ReadoutHotkeyKey(Key.I, 0x49, "I"),
        ReadoutHotkeyKey(Key.J, 0x4A, "J"),
        ReadoutHotkeyKey(Key.K, 0x4B, "K"),
        ReadoutHotkeyKey(Key.L, 0x4C, "L"),
        ReadoutHotkeyKey(Key.M, 0x4D, "M"),
        ReadoutHotkeyKey(Key.N, 0x4E, "N"),
        ReadoutHotkeyKey(Key.O, 0x4F, "O"),
        ReadoutHotkeyKey(Key.P, 0x50, "P"),
        ReadoutHotkeyKey(Key.Q, 0x51, "Q"),
        ReadoutHotkeyKey(Key.R, 0x52, "R"),
        ReadoutHotkeyKey(Key.S, 0x53, "S"),
        ReadoutHotkeyKey(Key.T, 0x54, "T"),
        ReadoutHotkeyKey(Key.U, 0x55, "U"),
        ReadoutHotkeyKey(Key.V, 0x56, "V"),
        ReadoutHotkeyKey(Key.W, 0x57, "W"),
        ReadoutHotkeyKey(Key.X, 0x58, "X"),
        ReadoutHotkeyKey(Key.Y, 0x59, "Y"),
        ReadoutHotkeyKey(Key.Z, 0x5A, "Z"),
        ReadoutHotkeyKey(Key.Zero, 0x30, "0"),
        ReadoutHotkeyKey(Key.One, 0x31, "1"),
        ReadoutHotkeyKey(Key.Two, 0x32, "2"),
        ReadoutHotkeyKey(Key.Three, 0x33, "3"),
        ReadoutHotkeyKey(Key.Four, 0x34, "4"),
        ReadoutHotkeyKey(Key.Five, 0x35, "5"),
        ReadoutHotkeyKey(Key.Six, 0x36, "6"),
        ReadoutHotkeyKey(Key.Seven, 0x37, "7"),
        ReadoutHotkeyKey(Key.Eight, 0x38, "8"),
        ReadoutHotkeyKey(Key.Nine, 0x39, "9"),
    )

internal fun readoutHotkeyVirtualKeyCode(key: Key): Int? = readoutHotkeyKeys.find { it.key == key }?.virtualKeyCode

internal fun readoutHotkeyDisplayName(virtualKeyCode: Int): String {
    val name = readoutHotkeyKeys.find { it.virtualKeyCode == virtualKeyCode }?.displayName ?: virtualKeyCode.toString()
    return "Ctrl + Alt + Shift + $name"
}
