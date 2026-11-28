@file:Suppress("FunctionNaming")

package kurou.kodriver.core.lmuwindowsdata.websocket

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEngineData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebSocketJsonTest {
    @Test
    fun `非有限値と未知のキーを含むJSONをデコードする`() {
        val result =
            webSocketJson.decodeFromString<LmuWindowsEngineData>(
                """{"rpm": NaN, "maxRpm": -Infinity, "gear": 1, "unknown": 1}""",
            )

        assertTrue(result.rpm.isNaN())
        assertEquals(Double.NEGATIVE_INFINITY, result.maxRpm)
    }
}
