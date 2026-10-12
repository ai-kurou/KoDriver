package kurou.kodriver.feature.gt7ps5debugstatedetail

import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5DebugStateCardKeyTest {
    @Test
    fun `GT7で使うキーだけを旧名で定義する`() {
        assertEquals(
            listOf("SIMULATOR", "VEHICLE_CLASS", "CURRENT_LAP", "BEST_LAP", "TYRE_TEMPERATURE", "FUEL_CONSUMPTION"),
            Gt7Ps5DebugStateCardKey.entries.map { it.name },
        )
        assertEquals(Gt7Ps5DebugStateCardKey.entries.toSet(), gt7Ps5SupportedCardKeys)
    }
}
