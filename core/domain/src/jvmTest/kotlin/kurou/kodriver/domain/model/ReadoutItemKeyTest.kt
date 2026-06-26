package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutItemKeyTest {

    @Test
    fun `fromValue は一致するキーを返す`() {
        assertEquals(ReadoutItemKey.VehicleApproach, ReadoutItemKey.fromValue("vehicle_approach"))
        assertEquals(ReadoutItemKey.Flag, ReadoutItemKey.fromValue("flag"))
        assertEquals(ReadoutItemKey.VehicleDamage, ReadoutItemKey.fromValue("vehicle_damage"))
        assertEquals(ReadoutItemKey.MyBestLap, ReadoutItemKey.fromValue("my_best_lap"))
        assertEquals(ReadoutItemKey.RemainingFuelLaps, ReadoutItemKey.fromValue("remaining_fuel_laps"))
    }

    @Test
    fun `fromValue は未知の値のとき null を返す`() {
        assertNull(ReadoutItemKey.fromValue("unknown"))
    }
}
