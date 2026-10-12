package kurou.kodriver.feature.acewindowsdebugstatedetail

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DebugStateMoveIndicesTest {
    @Test
    fun `キーから現在の順序を引き直し固定カードと未知キーを除外する`() {
        val order = defaultDebugStateCardOrder.reversed()
        assertEquals(0 to 1, debugStateMoveIndices(order, order[0].name, order[1].name))
        assertEquals(1 to 0, debugStateMoveIndices(order, order[1].name, order[0].name))
        assertEquals(0 to 0, debugStateMoveIndices(order, order[0].name, order[0].name))
        assertNull(debugStateMoveIndices(order, "debug_overlay_text_input", order[0].name))
        assertNull(debugStateMoveIndices(order, order[0].name, "debug_overlay_text_input"))
        assertNull(debugStateMoveIndices(emptyList(), "unknown", "unknown"))
    }
}
