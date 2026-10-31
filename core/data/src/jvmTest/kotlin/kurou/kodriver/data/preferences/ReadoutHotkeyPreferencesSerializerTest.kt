package kurou.kodriver.data.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutHotkeyPreferencesSerializerTest {
    @Test
    fun `デフォルト値はF1`() {
        assertEquals(
            ReadoutHotkeyPreferences(virtualKeyCode = 0x70),
            ReadoutHotkeyPreferencesSerializer.defaultValue,
        )
    }
}
