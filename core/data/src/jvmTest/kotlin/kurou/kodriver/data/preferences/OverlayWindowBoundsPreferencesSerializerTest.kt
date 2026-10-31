package kurou.kodriver.data.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

class OverlayWindowBoundsPreferencesSerializerTest {
    @Test
    fun `デフォルト値はOverlayWindowBoundsPreferencesの既定値と一致する`() {
        assertEquals(OverlayWindowBoundsPreferences(), OverlayWindowBoundsPreferencesSerializer.defaultValue)
    }
}
