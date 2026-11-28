package kurou.kodriver.core.designsystem

import androidx.compose.ui.input.key.Key

private data class HotkeyKey(
    val key: Key,
    val virtualKeyCode: Int,
    val displayName: String,
)

private val hotkeyKeys =
    listOf(
        HotkeyKey(Key.A, 0x41, "A"),
        HotkeyKey(Key.B, 0x42, "B"),
        HotkeyKey(Key.C, 0x43, "C"),
        HotkeyKey(Key.D, 0x44, "D"),
        HotkeyKey(Key.E, 0x45, "E"),
        HotkeyKey(Key.F, 0x46, "F"),
        HotkeyKey(Key.G, 0x47, "G"),
        HotkeyKey(Key.H, 0x48, "H"),
        HotkeyKey(Key.I, 0x49, "I"),
        HotkeyKey(Key.J, 0x4A, "J"),
        HotkeyKey(Key.K, 0x4B, "K"),
        HotkeyKey(Key.L, 0x4C, "L"),
        HotkeyKey(Key.M, 0x4D, "M"),
        HotkeyKey(Key.N, 0x4E, "N"),
        HotkeyKey(Key.O, 0x4F, "O"),
        HotkeyKey(Key.P, 0x50, "P"),
        HotkeyKey(Key.Q, 0x51, "Q"),
        HotkeyKey(Key.R, 0x52, "R"),
        HotkeyKey(Key.S, 0x53, "S"),
        HotkeyKey(Key.T, 0x54, "T"),
        HotkeyKey(Key.U, 0x55, "U"),
        HotkeyKey(Key.V, 0x56, "V"),
        HotkeyKey(Key.W, 0x57, "W"),
        HotkeyKey(Key.X, 0x58, "X"),
        HotkeyKey(Key.Y, 0x59, "Y"),
        HotkeyKey(Key.Z, 0x5A, "Z"),
        HotkeyKey(Key.Zero, 0x30, "0"),
        HotkeyKey(Key.One, 0x31, "1"),
        HotkeyKey(Key.Two, 0x32, "2"),
        HotkeyKey(Key.Three, 0x33, "3"),
        HotkeyKey(Key.Four, 0x34, "4"),
        HotkeyKey(Key.Five, 0x35, "5"),
        HotkeyKey(Key.Six, 0x36, "6"),
        HotkeyKey(Key.Seven, 0x37, "7"),
        HotkeyKey(Key.Eight, 0x38, "8"),
        HotkeyKey(Key.Nine, 0x39, "9"),
    )

/** ホットキーに割り当てられる数字・英字キーの仮想キーコードを返す。対象外のキーは null。 */
public fun hotkeyVirtualKeyCode(key: Key): Int? = hotkeyKeys.find { it.key == key }?.virtualKeyCode

/** 仮想キーコードの表示名を返す。対応表にないコードは数値をそのまま返す。 */
public fun hotkeyDisplayName(virtualKeyCode: Int): String =
    hotkeyKeys.find { it.virtualKeyCode == virtualKeyCode }?.displayName ?: virtualKeyCode.toString()
