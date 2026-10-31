package kurou.kodriver.data.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

class OverlayBackgroundOpacityPreferencesSerializerTest {
    @Test
    fun `デフォルト値はOverlayBackgroundOpacityPreferencesの既定値と一致する`() {
        assertEquals(OverlayBackgroundOpacityPreferences(), OverlayBackgroundOpacityPreferencesSerializer.defaultValue)
    }
}
