package kurou.kodriver.data.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

class LowVolumeWarningEnabledPreferencesSerializerTest {
    @Test
    fun `デフォルト値はLowVolumeWarningEnabledPreferencesの既定値と一致する`() {
        assertEquals(LowVolumeWarningEnabledPreferences(), LowVolumeWarningEnabledPreferencesSerializer.defaultValue)
    }
}
