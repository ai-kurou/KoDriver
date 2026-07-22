package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DebugStateCardKeyTest {

    @Test
    fun `SIMULATOR・FLAG_INFO・GAME_PHASE・SESSION・YELLOW_FLAG_STATEの5種類が定義されている`() {
        assertEquals(
            listOf(
                DebugStateCardKey.SIMULATOR,
                DebugStateCardKey.FLAG_INFO,
                DebugStateCardKey.GAME_PHASE,
                DebugStateCardKey.SESSION,
                DebugStateCardKey.YELLOW_FLAG_STATE,
            ),
            DebugStateCardKey.entries,
        )
    }
}
