package kurou.kodriver.domain.model

import kurou.kodriver.domain.engine.DebugReadoutText
import kotlin.test.Test
import kotlin.test.assertEquals

class DebugReadoutRequestTest {
    @Test
    fun `デバッグ文言と置換後の文言は入力をそのまま保持する`() {
        val key =
            object : ReadoutItemKey.TopLevel {
                override val value = "test"
                override val supportsQueue = true
            }
        val event = DebugReadoutText(key, " {laps} ")
        assertEquals(" {laps} ", event.narratedText)
        assertEquals(" {laps} ", event.resolvedText)
        assertEquals(DebugReadoutText(key, "変更"), event.withResolvedText("変更"))
        assertEquals("変更", event.withResolvedText("変更").narratedText)
    }
}
