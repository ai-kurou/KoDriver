package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutHotkeyModifiersDisplayNameTest {
    @Test
    fun `修飾キーの表示名を検証する`() {
        assertEquals(
            listOf("Ctrl + Alt", "Ctrl + Shift", "Alt + Shift"),
            ManualReadoutHotkeyModifiers.entries.map { it.displayName() },
        )
    }
}
