package kurou.kodriver.feature.manualreadoutlist

import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ManualReadoutHotkeyLabelTest {
    @Test
    fun `全修飾キーで英字と数字と範囲外を空白なしで表示し未設定はnullを返す`() {
        val prefixes =
            mapOf(
                ManualReadoutHotkeyModifiers.CTRL_ALT to "Ctrl+Alt",
                ManualReadoutHotkeyModifiers.CTRL_SHIFT to "Ctrl+Shift",
                ManualReadoutHotkeyModifiers.ALT_SHIFT to "Alt+Shift",
            )
        val keys =
            "0123456789".mapIndexed { index, key -> 0x30 + index to key.toString() }.toMap() +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ".mapIndexed { index, key -> 0x41 + index to key.toString() }.toMap() +
                listOf(-1, 0x2F, 0x3A, 0x40, 0x5B).associateWith { it.toString() }
        prefixes.forEach { (modifiers, prefix) ->
            keys.forEach { (code, name) ->
                assertEquals("$prefix+$name", manualReadoutHotkeyLabel(modifiers, code))
            }
            assertNull(manualReadoutHotkeyLabel(modifiers, null))
        }
    }
}
