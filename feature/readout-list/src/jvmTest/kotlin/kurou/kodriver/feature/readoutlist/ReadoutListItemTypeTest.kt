package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutListItemTypeTest {

    @Test
    fun `lmu_windows の vehicle_approach は LmuWindows_VehicleApproach を返す`() {
        assertEquals(
            ReadoutListItemType.LmuWindows.VehicleApproach,
            ReadoutListItemType.fromId("lmu_windows", ReadoutItemKey.VEHICLE_APPROACH),
        )
    }

    @Test
    fun `lmu_windows の flag は LmuWindows_Flag を返す`() {
        assertEquals(
            ReadoutListItemType.LmuWindows.Flag,
            ReadoutListItemType.fromId("lmu_windows", ReadoutItemKey.FLAG),
        )
    }

    @Test
    fun `lmu_windows の vehicle_damage は LmuWindows_VehicleDamage を返す`() {
        assertEquals(
            ReadoutListItemType.LmuWindows.VehicleDamage,
            ReadoutListItemType.fromId("lmu_windows", ReadoutItemKey.VEHICLE_DAMAGE),
        )
    }

    @Test
    fun `gt7_ps5 の best_lap は Gt7Ps5_BestLap を返す`() {
        assertEquals(
            ReadoutListItemType.Gt7Ps5.MyBestLap,
            ReadoutListItemType.fromId("gt7_ps5", ReadoutItemKey.MY_BEST_LAP),
        )
    }

    @Test
    fun `gt7_ps5 の remaining_fuel_laps は Gt7Ps5_RemainingFuelLaps を返す`() {
        assertEquals(
            ReadoutListItemType.Gt7Ps5.RemainingFuelLaps,
            ReadoutListItemType.fromId("gt7_ps5", ReadoutItemKey.REMAINING_FUEL_LAPS),
        )
    }

    @Test
    fun `lmu_windows でシミュレータに属さないキーは null を返す`() {
        assertNull(ReadoutListItemType.fromId("lmu_windows", ReadoutItemKey.MY_BEST_LAP))
    }

    @Test
    fun `gt7_ps5 でシミュレータに属さないキーは null を返す`() {
        assertNull(ReadoutListItemType.fromId("gt7_ps5", ReadoutItemKey.FLAG))
    }

    @Test
    fun `未知のシミュレータは null を返す`() {
        assertNull(ReadoutListItemType.fromId("unknown", ReadoutItemKey.FLAG))
    }
}
