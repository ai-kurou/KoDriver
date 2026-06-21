package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutItemKeyTest {

    @Test
    fun `fromValue は一致するキーを返す`() {
        assertEquals(ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.fromValue("vehicle_approach"))
        assertEquals(ReadoutItemKey.FLAG, ReadoutItemKey.fromValue("flag"))
        assertEquals(ReadoutItemKey.VEHICLE_DAMAGE, ReadoutItemKey.fromValue("vehicle_damage"))
        assertEquals(ReadoutItemKey.BEST_LAP, ReadoutItemKey.fromValue("best_lap"))
    }

    @Test
    fun `fromValue は未知の値のとき null を返す`() {
        assertNull(ReadoutItemKey.fromValue("unknown"))
    }
}
