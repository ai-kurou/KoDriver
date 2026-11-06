package kurou.kodriver.core.speechrecognitiondata.speech

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VoskSpeechTranscriberTest {
    @Test
    fun `Voskの文字列変換にはUTF-8を指定する`() {
        val previous = System.getProperty(JNA_ENCODING_PROPERTY)
        try {
            System.setProperty(JNA_ENCODING_PROPERTY, "MS932")

            useUtf8ForVoskStrings()

            assertEquals("UTF-8", System.getProperty(JNA_ENCODING_PROPERTY))
        } finally {
            if (previous == null) {
                System.clearProperty(JNA_ENCODING_PROPERTY)
            } else {
                System.setProperty(JNA_ENCODING_PROPERTY, previous)
            }
        }
    }

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
    fun `確定結果の候補は入力順ではなく信頼度の高い順に並べる`() {
        val json =
            """
            {"alternatives":[
              {"text":"b","confidence":0.2},
              {"text":"a","confidence":0.8},
              {"text":"c"}
            ]}
            """.trimIndent()

        assertEquals(SpeechTranscript.Final(listOf("a", "b", "c")), parseVoskFinal(json))
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
