package kurou.kodriver.data.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

class VoicePreferencesSerializerTest {
    @Test
    fun `デフォルト値はVoicePreferencesの既定値と一致する`() {
        assertEquals(VoicePreferences(), VoicePreferencesSerializer.defaultValue)
    }
}
