package kurou.kodriver.core.texttospeechdata.windows

import kurou.kodriver.domain.model.TextToSpeechVoice
import kotlin.test.Test
import kotlin.test.assertEquals

class VoiceListParserTest {
    @Test
    fun `空行と列不足を除外し空白とCRLFを処理する`() {
        val output =
            "\r\n  \r\n不足\r\n名前\t説明\r\n  SAPI Name \t 日本語音声 \t ja-JP \r\n" +
                "English\tEnglish Voice\ten-US\textra\n"

        assertEquals(
            listOf(
                TextToSpeechVoice("SAPI Name", "日本語音声", "ja-JP"),
                TextToSpeechVoice("English", "English Voice", "en-US"),
            ),
            parseVoiceList(output),
        )
        assertEquals(emptyList(), parseVoiceList(""))
    }

    @Test
    fun `先頭のBOMを取り除く`() {
        assertEquals(
            listOf(TextToSpeechVoice("SAPI Name", "日本語音声", "ja-JP")),
            parseVoiceList("\uFEFFSAPI Name\t日本語音声\tja-JP\n"),
        )
    }
}
