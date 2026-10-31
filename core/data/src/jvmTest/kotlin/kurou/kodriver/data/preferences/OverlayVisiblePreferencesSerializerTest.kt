package kurou.kodriver.data.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

class OverlayVisiblePreferencesSerializerTest {
    @Test
    fun `デフォルト値はOverlayVisiblePreferencesの既定値と一致する`() {
        assertEquals(OverlayVisiblePreferences(), OverlayVisiblePreferencesSerializer.defaultValue)
    }
}
