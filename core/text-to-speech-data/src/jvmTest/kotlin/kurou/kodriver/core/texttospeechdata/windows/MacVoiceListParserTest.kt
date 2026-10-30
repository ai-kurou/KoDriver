package kurou.kodriver.core.texttospeechdata.windows

import kurou.kodriver.domain.model.TextToSpeechVoice
import kotlin.test.Test
import kotlin.test.assertEquals

class MacVoiceListParserTest {
    @Test
    fun `日本語音声だけを解析する`() {
        val output =
            "Kyoko               ja_JP    # こんにちは!\n" +
                "Eddy (日本語（日本）)      ja_JP    # こんにちは!\n" +
                "Alex                en_US    # Hello!\n" +
                "\n" +
                "壊れた行\n"
        assertEquals(
            listOf(
                TextToSpeechVoice("Kyoko", "Kyoko", "ja-JP", false),
                TextToSpeechVoice("Eddy (日本語（日本）)", "Eddy (日本語（日本）)", "ja-JP", false),
            ),
            parseMacVoiceList(output),
        )
    }

    @Test
    fun `空の出力は空の一覧になる`() {
        assertEquals(emptyList(), parseMacVoiceList(""))
    }
}
