package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutListItemTypeTest {

    @Test
    fun `lmu_windows の vehicle_approach は LmuWindows_VehicleApproach を返す`() {
        assertEquals(
            ReadoutListItemType.LmuWindows.VehicleApproach,
            ReadoutListItemType.fromId(Simulator.LmuWindows, ReadoutItemKey.VehicleApproach),
        )
    }

    @Test
    fun `lmu_windows の flag は LmuWindows_Flag を返す`() {
        assertEquals(
            ReadoutListItemType.LmuWindows.Flag,
            ReadoutListItemType.fromId(Simulator.LmuWindows, ReadoutItemKey.Flag),
        )
    }

    @Test
    fun `lmu_windows の vehicle_damage は LmuWindows_VehicleDamage を返す`() {
        assertEquals(
            ReadoutListItemType.LmuWindows.VehicleDamage,
            ReadoutListItemType.fromId(Simulator.LmuWindows, ReadoutItemKey.VehicleDamage),
        )
    }

    @Test
    fun `lmu_windows の my_best_lap は LmuWindows_MyBestLap を返す`() {
        assertEquals(
            ReadoutListItemType.LmuWindows.MyBestLap,
            ReadoutListItemType.fromId(Simulator.LmuWindows, ReadoutItemKey.MyBestLap),
        )
    }

    @Test
    fun `gt7_ps5 の best_lap は Gt7Ps5_BestLap を返す`() {
        assertEquals(
            ReadoutListItemType.Gt7Ps5.MyBestLap,
            ReadoutListItemType.fromId(Simulator.Gt7Ps5, ReadoutItemKey.MyBestLap),
        )
    }

    @Test
    fun `gt7_ps5 の remaining_fuel_laps は Gt7Ps5_RemainingFuelLaps を返す`() {
        assertEquals(
            ReadoutListItemType.Gt7Ps5.RemainingFuelLaps,
            ReadoutListItemType.fromId(Simulator.Gt7Ps5, ReadoutItemKey.RemainingFuelLaps),
        )
    }

    @Test
    fun `lmu_windows でシミュレータに属さないキーは null を返す`() {
        assertNull(ReadoutListItemType.fromId(Simulator.LmuWindows, ReadoutItemKey.RemainingFuelLaps))
    }

    @Test
    fun `lmu_windows の tyre_temperature は TyreTemperature を返す`() {
        assertEquals(
            ReadoutListItemType.LmuWindows.TyreTemperature,
            ReadoutListItemType.fromId(Simulator.LmuWindows, ReadoutItemKey.TyreTemperature),
        )
    }

    @Test
    fun `gt7_ps5 でシミュレータに属さないキーは null を返す`() {
        assertNull(ReadoutListItemType.fromId(Simulator.Gt7Ps5, ReadoutItemKey.Flag))
    }
}
