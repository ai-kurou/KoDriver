package kurou.kodriver.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpeechRecognitionCorrectionsTest {
    @Test
    fun `登録された置換は依頼された4組である`() {
        assertEquals(
            listOf("産業" to "残量", "集会" to "周回", "まーも" to "摩耗", "ポンド" to "温度"),
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
