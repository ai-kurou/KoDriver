package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DebugStateCardKeyTest {
    @Test
    fun `SIMULATOR等13種類が定義されている`() {
        assertEquals(
            listOf(
                DebugStateCardKey.SIMULATOR,
                DebugStateCardKey.FLAG_INFO,
                DebugStateCardKey.GAME_PHASE,
                DebugStateCardKey.SESSION,
                DebugStateCardKey.YELLOW_FLAG_STATE,
                DebugStateCardKey.CURRENT_LAP,
                DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
                DebugStateCardKey.BEST_LAP,
                DebugStateCardKey.TYRE_TEMPERATURE,
                DebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
                DebugStateCardKey.TYRE_WEAR,
                DebugStateCardKey.FUEL_CONSUMPTION,
                DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
            ),
            DebugStateCardKey.entries,
        )
    }
}
