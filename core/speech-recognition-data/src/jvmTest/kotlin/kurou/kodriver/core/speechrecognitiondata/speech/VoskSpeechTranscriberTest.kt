package kurou.kodriver.core.speechrecognitiondata.speech

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VoskSpeechTranscriberTest {
    @Test
    fun `途中経過のJSONから文字列を取り出し、空白は取り除く`() {
        assertEquals(SpeechTranscript.Partial("次へ"), parseVoskPartial("""{"partial":"次 へ"}"""))
    }

    @Test
    fun `途中経過が空なら無しとして扱う`() {
        assertNull(parseVoskPartial("""{"partial":""}"""))
        assertNull(parseVoskPartial("""{"partial":"  "}"""))
        assertNull(parseVoskPartial("""{}"""))
    }

    @Test
    fun `確定結果のJSONから候補を信頼度の高い順に取り出し、空白と重複を取り除く`() {
        val json =
            """
            {"alternatives":[
              {"text":"次 へ","confidence":0.9},
              {"text":"つぎ へ","confidence":0.5},
              {"text":"次へ","confidence":0.3},
              {"text":"","confidence":0.1}
            ]}
            """.trimIndent()

        assertEquals(SpeechTranscript.Final(listOf("次へ", "つぎへ")), parseVoskFinal(json))
    }

    @Test
    fun `確定結果に候補が無い・空文字だけなら無しとして扱う`() {
        assertNull(parseVoskFinal("""{"alternatives":[]}"""))
        assertNull(parseVoskFinal("""{"alternatives":[{"text":""}]}"""))
        assertNull(parseVoskFinal("""{"alternatives":[{"confidence":0.5}]}"""))
        assertNull(parseVoskFinal("""{}"""))
    }

    @Test
    fun `モデルのディレクトリがあるときだけ利用できる`() {
        val directory = Files.createTempDirectory("vosk-model-test").toFile()
        try {
            assertTrue(VoskSpeechTranscriberFactory(directory).isAvailable())
            directory.delete()
            assertFalse(VoskSpeechTranscriberFactory(directory).isAvailable())
        } finally {
            directory.delete()
        }
    }
}
