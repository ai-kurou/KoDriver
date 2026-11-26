package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class MicrophoneCutQuietSoundThresholdTest {
    @Test
    fun `段階値を閾値にする`() {
        assertEquals(0f, MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN.toMicrophoneCutQuietSoundThreshold())
        assertEquals(0.15f, MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT.toMicrophoneCutQuietSoundThreshold(), 0.0001f)
        assertEquals(0.6f, MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX.toMicrophoneCutQuietSoundThreshold(), 0.0001f)
    }
}
