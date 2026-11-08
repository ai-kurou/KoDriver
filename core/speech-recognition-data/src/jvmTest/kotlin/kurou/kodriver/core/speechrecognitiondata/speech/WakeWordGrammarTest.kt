package kurou.kodriver.core.speechrecognitiondata.speech

import kotlin.test.Test
import kotlin.test.assertEquals

class WakeWordGrammarTest {
    @Test
    fun `指定した語と未知語をJSONの文字列配列にする`() {
        assertEquals("""["どらいばー","ドライバー","[unk]"]""", wakeWordGrammar(listOf("どらいばー", "ドライバー")))
    }

    @Test
    fun `既定ではウェイクワードの全表記と未知語を含む`() {
        assertEquals(
            """["コドライバー","こどらいばー","このドライバー","このどらいばー","[unk]"]""",
            wakeWordGrammar(),
        )
    }
}
