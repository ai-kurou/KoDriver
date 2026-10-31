package kurou.kodriver.data.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

class SoundVolumePreferencesSerializerTest {
    @Test
    fun `デフォルト値はSoundVolumePreferencesの既定値と一致する`() {
        assertEquals(SoundVolumePreferences(), SoundVolumePreferencesSerializer.defaultValue)
    }
}
