package kurou.kodriver.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpeechRecognitionCorrectionsTest {
    @Test
    fun `登録された置換は依頼された17組である`() {
        assertEquals(
            listOf(
                "産業" to "残量",
                "集会" to "周回",
                "まーも" to "摩耗",
                "ポンド" to "温度",
                "方法" to "後方",
                "なぁ集" to "何周",
                "なぁ州" to "何周",
                "なぁ秋" to "何周",
                "何シュー" to "何周",
                "たい山も" to "タイヤ摩耗",
                "大山も" to "タイヤ摩耗",
                "残業" to "残量",
                "音頭" to "温度",
                "全校" to "前後",
                "前項" to "前後",
                "善行" to "前後",
                "全高" to "前後",
            ),
            SPEECH_RECOGNITION_CORRECTIONS,
        )
    }

    @Test
    fun `置換元は空白を含まず空でない`() {
        assertTrue(
            SPEECH_RECOGNITION_CORRECTIONS.all { (wrong, _) ->
                wrong.isNotEmpty() && wrong.none { it.isWhitespace() }
            },
        )
    }
}
