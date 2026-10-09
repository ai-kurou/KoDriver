package kurou.kodriver.core.texttospeechdata.windows

import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResidentSpeakScriptTest {
    @Test
    fun `要求行は音量と音声IDとテキストをBase64で並べる`() {
        val request = buildSpeakRequest("It's a lap\n試聴", 30, "voice 'a'")

        val parts = request.split(" ")
        assertEquals(4, parts.size)
        assertEquals("SPEAK", parts[0])
        assertEquals("30", parts[1])
        assertEquals("voice 'a'", String(Base64.getDecoder().decode(parts[2]), Charsets.UTF_8))
        assertEquals("It's a lap\n試聴", String(Base64.getDecoder().decode(parts[3]), Charsets.UTF_8))
        assertFalse(request.contains("\n"))
    }

    @Test
    fun `音声未指定の要求行は音声ID欄が空になる`() {
        val parts = buildSpeakRequest("試聴", 100, VOICE_ID_UNSPECIFIED).split(" ")

        assertEquals(4, parts.size)
        assertEquals("", parts[2])
    }

    @Test
    fun `常駐スクリプトは音声合成を1度だけ生成して応答行と日本語フォールバックを持つ`() {
        val script = buildResidentSpeakScript()

        assertEquals(1, Regex("New-Object System.Speech.Synthesis.SpeechSynthesizer").findAll(script).count())
        assertTrue(script.contains("GetCultureInfo('ja-JP')"))
        assertTrue(script.contains("WriteLine('$SPEAK_RESPONSE_DONE')"))
        assertTrue(script.contains("StartsWith('SPEAK ')"))
        assertTrue(script.contains("SpeakAsyncCancelAll"))
    }

    @Test
    fun `スクリプトはUTF16LEのBase64で渡す`() {
        val encoded = encodeResidentSpeakScript("Write-Output 'あ'")

        assertEquals("Write-Output 'あ'", String(Base64.getDecoder().decode(encoded), Charsets.UTF_16LE))
    }

    @Test
    fun `引数なしのエンコードは常駐スクリプトを対象にする`() {
        assertEquals(
            buildResidentSpeakScript(),
            String(Base64.getDecoder().decode(encodeResidentSpeakScript()), Charsets.UTF_16LE),
        )
    }
}
