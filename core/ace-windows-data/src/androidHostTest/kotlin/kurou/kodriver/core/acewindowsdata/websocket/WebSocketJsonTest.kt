@file:Suppress("FunctionNaming")

package kurou.kodriver.core.acewindowsdata.websocket

import kurou.kodriver.domain.acewindows.model.AceWindowsGapData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebSocketJsonTest {
    @Test
    fun `非有限値と未知のキーを含むJSONをデコードする`() {
        val result =
            webSocketJson.decodeFromString<AceWindowsGapData>(
                """{"gapAheadSeconds": NaN, "gapBehindSeconds": -Infinity, "unknown": 1}""",
            )

        assertTrue(result.gapAheadSeconds.isNaN())
        assertEquals(Float.NEGATIVE_INFINITY, result.gapBehindSeconds)
    }
}
