package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidAudioUsageTest {
    @Test
    fun `各IDを用途に変換できる`() {
        assertEquals(AndroidAudioUsage.MEDIA, AndroidAudioUsage.fromId("media"))
        assertEquals(AndroidAudioUsage.COMMUNICATION, AndroidAudioUsage.fromId("communication"))
        assertEquals(AndroidAudioUsage.RINGTONE, AndroidAudioUsage.fromId("ringtone"))
        assertEquals(AndroidAudioUsage.NOTIFICATION, AndroidAudioUsage.fromId("notification"))
        assertEquals(AndroidAudioUsage.ALARM, AndroidAudioUsage.fromId("alarm"))
        assertEquals(AndroidAudioUsage.ASSISTANT, AndroidAudioUsage.fromId("assistant"))
    }

    @Test
    fun `未知IDは既定値のメディアに変換される`() {
        assertEquals(ANDROID_AUDIO_USAGE_DEFAULT, AndroidAudioUsage.fromId("unknown"))
        assertEquals(AndroidAudioUsage.MEDIA, ANDROID_AUDIO_USAGE_DEFAULT)
    }
}
