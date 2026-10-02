package kurou.kodriver.core.texttospeechdata.windows

import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kotlin.test.Test
import kotlin.test.assertEquals

class SpeakScriptTest {
    private val prefix =
        "Add-Type -AssemblyName System.Speech; " +
            "\$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; "
    private val fallback =
        "\$s.SelectVoiceByHints(" +
            "[System.Speech.Synthesis.VoiceGender]::NotSet, " +
            "[System.Speech.Synthesis.VoiceAge]::NotSet, 0, " +
            "[System.Globalization.CultureInfo]::GetCultureInfo('ja-JP'))"

    @Test
    fun `音声未指定は従来どおり日本語音声だけを選択する`() {
        assertEquals(
            prefix + "\$s.Volume = 100; $fallback; \$s.Speak('試聴')",
            buildSpeakScript("試聴", 100, VOICE_ID_UNSPECIFIED),
        )
    }

    @Test
    fun `指定音声を選択し失敗時は日本語音声へフォールバックする`() {
        assertEquals(
            prefix + "\$s.Volume = 30; " +
                "try { \$s.SelectVoice('voice-a') } catch { $fallback }; \$s.Speak('試聴')",
            buildSpeakScript("試聴", 30, "voice-a"),
        )
    }

    @Test
    fun `音声IDとテキストの単一引用符をエスケープする`() {
        assertEquals(
            prefix + "\$s.Volume = 0; " +
                "try { \$s.SelectVoice('voice''a') } catch { $fallback }; \$s.Speak('It''s a lap')",
            buildSpeakScript("It's a lap", 0, "voice'a"),
        )
    }

    @Test
    fun `タイポグラフィ引用符も二重化して文字列から抜け出せないようにする`() {
        assertEquals(
            prefix + "\$s.Volume = 50; " +
                "try { \$s.SelectVoice('a\u2019\u2019); x') } catch { $fallback }; " +
                "\$s.Speak('\u2018\u2018\u201A\u201A\u201B\u201B')",
            buildSpeakScript("\u2018\u201A\u201B", 50, "a\u2019); x"),
        )
    }
}
