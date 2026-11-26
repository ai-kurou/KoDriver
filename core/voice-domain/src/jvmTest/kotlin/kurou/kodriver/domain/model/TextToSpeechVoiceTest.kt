package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TextToSpeechVoiceTest {
    @Test
    fun `音声の各項目を保持しコピーと値の比較ができる`() {
        val voice = TextToSpeechVoice("SAPI Name", "表示名", "ja-JP")
        val (id, displayName, cultureName, isDefault) = voice

        assertEquals("SAPI Name", id)
        assertEquals("表示名", displayName)
        assertEquals(TTS_CULTURE_NAME, cultureName)
        assertFalse(isDefault)
        assertTrue(voice.copy(isDefault = true).isDefault)
        assertNotEquals(voice, voice.copy(isDefault = true))
        assertEquals(TextToSpeechVoice(id, displayName, cultureName), voice)
        assertEquals(voice.hashCode(), voice.copy().hashCode())
        assertNotEquals(voice, voice.copy(id = "別の音声"))
        assertEquals(
            "TextToSpeechVoice(id=SAPI Name, displayName=表示名, cultureName=ja-JP, isDefault=false)",
            voice.toString(),
        )
    }
}
