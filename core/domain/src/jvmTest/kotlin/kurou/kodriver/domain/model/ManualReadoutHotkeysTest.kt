package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutHotkeysTest {
    @Test
    fun `数字と英字だけを選択できる`() {
        (-1..0x100).forEach { code ->
            assertEquals(code in 0x30..0x39 || code in 0x41..0x5A, isManualReadoutHotkeyVirtualKeyCode(code))
        }
    }
}
