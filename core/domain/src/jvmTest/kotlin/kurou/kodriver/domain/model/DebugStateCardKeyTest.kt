package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DebugStateCardKeyTest {

    @Test
    fun `SIMULATOR・FLAG_INFO・GAME_PHASE・SESSIONの4種類が定義されている`() {
        assertEquals(
            listOf(
                DebugStateCardKey.SIMULATOR,
                DebugStateCardKey.FLAG_INFO,
                DebugStateCardKey.GAME_PHASE,
                DebugStateCardKey.SESSION,
            ),
            DebugStateCardKey.entries,
        )
    }
}
