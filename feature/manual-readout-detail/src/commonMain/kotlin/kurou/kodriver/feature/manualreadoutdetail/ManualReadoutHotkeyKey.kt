package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.ui.input.key.Key
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers

private data class ManualReadoutHotkeyKey(
    val key: Key,
    val virtualKeyCode: Int,
    val displayName: String,
)

private val manualReadoutHotkeyKeys =
    listOf(
        ManualReadoutHotkeyKey(Key.A, 0x41, "A"),
        ManualReadoutHotkeyKey(Key.B, 0x42, "B"),
        ManualReadoutHotkeyKey(Key.C, 0x43, "C"),
        ManualReadoutHotkeyKey(Key.D, 0x44, "D"),
        ManualReadoutHotkeyKey(Key.E, 0x45, "E"),
        ManualReadoutHotkeyKey(Key.F, 0x46, "F"),
        ManualReadoutHotkeyKey(Key.G, 0x47, "G"),
        ManualReadoutHotkeyKey(Key.H, 0x48, "H"),
        ManualReadoutHotkeyKey(Key.I, 0x49, "I"),
        ManualReadoutHotkeyKey(Key.J, 0x4A, "J"),
        ManualReadoutHotkeyKey(Key.K, 0x4B, "K"),
        ManualReadoutHotkeyKey(Key.L, 0x4C, "L"),
        ManualReadoutHotkeyKey(Key.M, 0x4D, "M"),
        ManualReadoutHotkeyKey(Key.N, 0x4E, "N"),
        ManualReadoutHotkeyKey(Key.O, 0x4F, "O"),
        ManualReadoutHotkeyKey(Key.P, 0x50, "P"),
        ManualReadoutHotkeyKey(Key.Q, 0x51, "Q"),
        ManualReadoutHotkeyKey(Key.R, 0x52, "R"),
        ManualReadoutHotkeyKey(Key.S, 0x53, "S"),
        ManualReadoutHotkeyKey(Key.T, 0x54, "T"),
        ManualReadoutHotkeyKey(Key.U, 0x55, "U"),
        ManualReadoutHotkeyKey(Key.V, 0x56, "V"),
        ManualReadoutHotkeyKey(Key.W, 0x57, "W"),
        ManualReadoutHotkeyKey(Key.X, 0x58, "X"),
        ManualReadoutHotkeyKey(Key.Y, 0x59, "Y"),
        ManualReadoutHotkeyKey(Key.Z, 0x5A, "Z"),
        ManualReadoutHotkeyKey(Key.Zero, 0x30, "0"),
        ManualReadoutHotkeyKey(Key.One, 0x31, "1"),
        ManualReadoutHotkeyKey(Key.Two, 0x32, "2"),
        ManualReadoutHotkeyKey(Key.Three, 0x33, "3"),
        ManualReadoutHotkeyKey(Key.Four, 0x34, "4"),
        ManualReadoutHotkeyKey(Key.Five, 0x35, "5"),
        ManualReadoutHotkeyKey(Key.Six, 0x36, "6"),
        ManualReadoutHotkeyKey(Key.Seven, 0x37, "7"),
        ManualReadoutHotkeyKey(Key.Eight, 0x38, "8"),
        ManualReadoutHotkeyKey(Key.Nine, 0x39, "9"),
    )

internal fun manualReadoutHotkeyVirtualKeyCode(key: Key): Int? =
    manualReadoutHotkeyKeys.find { it.key == key }?.virtualKeyCode

internal fun manualReadoutHotkeyDisplayName(virtualKeyCode: Int): String =
    manualReadoutHotkeyKeys.find { it.virtualKeyCode == virtualKeyCode }?.displayName ?: virtualKeyCode.toString()

internal fun ManualReadoutHotkeyModifiers.displayName(): String =
    when (this) {
        ManualReadoutHotkeyModifiers.CTRL_ALT -> "Ctrl + Alt"
        ManualReadoutHotkeyModifiers.CTRL_SHIFT -> "Ctrl + Shift"
        ManualReadoutHotkeyModifiers.ALT_SHIFT -> "Alt + Shift"
    }
