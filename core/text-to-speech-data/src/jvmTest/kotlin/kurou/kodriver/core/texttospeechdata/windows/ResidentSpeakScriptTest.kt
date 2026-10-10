package kurou.kodriver.core.texttospeechdata.windows

import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResidentSpeakScriptTest {
    @Test
    fun `要求行は音量とRateとピッチ半音とBase64の音声IDとテキストを並べる`() {
        val request = buildSpeakRequest("It's a lap\n試聴", 30, "voice 'a'", 6, 2.0f)

        val parts = request.split(" ")
        assertEquals(6, parts.size)
        assertEquals("SPEAK", parts[0])
        assertEquals("30", parts[1])
        assertEquals("6", parts[2])
        assertEquals("+12.0", parts[3])
        assertTrue(request.all { it.code < 128 })
        assertEquals("voice 'a'", String(Base64.getDecoder().decode(parts[4]), Charsets.UTF_8))
        assertEquals("It's a lap\n試聴", String(Base64.getDecoder().decode(parts[5]), Charsets.UTF_8))
        assertFalse(request.contains("\n"))
    }

    @Test
    fun `音声未指定の要求行は音声ID欄が空になる`() {
        val parts = buildSpeakRequest("試聴", 100, VOICE_ID_UNSPECIFIED, 0).split(" ")

        assertEquals(6, parts.size)
        assertEquals("0.0", parts[3])
        assertEquals("", parts[4])
    }

    @Test
    fun `低いピッチとXML特殊文字もASCIIの要求行で渡す`() {
        val text = "<tag>&\"'試聴"
        val request = buildSpeakRequest(text, 42, "voice-a", 0, 0.5f)
        val parts = request.split(" ")

        assertEquals("-12.0", parts[3])
        assertEquals(text, String(Base64.getDecoder().decode(parts[5]), Charsets.UTF_8))
        assertTrue(request.all { it.code < 128 })
    }

    @Test
    fun `常駐スクリプトは音声合成を1度だけ生成して応答行と日本語フォールバックを持つ`() {
        val script = buildResidentSpeakScript()

        assertEquals(1, Regex("New-Object System.Speech.Synthesis.SpeechSynthesizer").findAll(script).count())
        assertTrue(script.contains("GetCultureInfo('ja-JP')"))
        assertTrue(script.contains("WriteLine('$SPEAK_RESPONSE_DONE')"))
        assertTrue(script.contains("StartsWith('SPEAK ')"))
        assertTrue(script.contains("SpeakAsyncCancelAll"))
        assertTrue(script.contains("${'$'}s.Rate = [int]${'$'}parts[2]"))
        assertTrue(script.contains("FromBase64String(${'$'}parts[5])"))
        assertTrue(script.contains("FromBase64String(${'$'}parts[4])"))
    }

    @Test
    fun `標準ピッチは通常発話しそれ以外はXMLエスケープしたSSMLで発話する`() {
        val script = buildResidentSpeakScript()

        assertTrue(script.contains("${'$'}pitch = ${'$'}parts[3]"))
        assertTrue(script.contains("if (${'$'}pitch -eq '0.0')"))
        assertTrue(script.contains("${'$'}s.SpeakAsync(${'$'}text)"))
        assertTrue(script.contains("[System.Security.SecurityElement]::Escape(${'$'}text)"))
        assertTrue(script.contains("${'$'}s.SpeakSsmlAsync(${'$'}ssml)"))
        assertTrue(
            script.contains(
                "<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" " +
                    "xml:lang=\"' + ${'$'}s.Voice.Culture.Name",
            ),
        )
        assertTrue(script.contains("<prosody pitch=\""))
        assertTrue(script.contains("st\">"))
        assertTrue(script.contains("</prosody></speak>"))
    }

    @Test
    fun `ピッチ倍率は半音へ変換し上下限に制限する`() {
        assertEquals("-12.0", pitchToSemitones(0.5f))
        assertEquals("0.0", pitchToSemitones(1.0f))
        assertEquals("+12.0", pitchToSemitones(2.0f))
        assertEquals("-12.0", pitchToSemitones(0.1f))
        assertEquals("+12.0", pitchToSemitones(3.0f))
        assertEquals("+7.02", pitchToSemitones(1.5f))
        assertEquals("0.0", pitchToSemitones(1.0000001f))
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

    @Test
    fun `速度倍率は対数スケールの整数Rateへ変換し上下限に丸める`() {
        assertEquals(-6, speedToSapiRate(0.5f))
        assertEquals(0, speedToSapiRate(1.0f))
        assertEquals(6, speedToSapiRate(2.0f))
        assertEquals(-10, speedToSapiRate(0.1f))
        assertEquals(10, speedToSapiRate(10.0f))
    }
}
