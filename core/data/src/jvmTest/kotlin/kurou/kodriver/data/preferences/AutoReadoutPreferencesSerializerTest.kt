package kurou.kodriver.data.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

class AutoReadoutPreferencesSerializerTest {
    @Test
    fun `デフォルト値はAutoReadoutPreferencesの既定値と一致する`() {
        assertEquals(AutoReadoutPreferences(), AutoReadoutPreferencesSerializer.defaultValue)
    }
}
