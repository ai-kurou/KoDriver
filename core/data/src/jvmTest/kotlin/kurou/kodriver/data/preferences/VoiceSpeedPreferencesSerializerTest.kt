package kurou.kodriver.data.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

class VoiceSpeedPreferencesSerializerTest {
    @Test
    fun `デフォルト値は1_0`() {
        assertEquals(1.0f, VoiceSpeedPreferencesSerializer.defaultValue.voiceSpeed)
    }
}
