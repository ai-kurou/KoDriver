package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutListItemTypeFromIdTest {
    @Test
    fun `lmu_windows の vehicle_approach は LmuWindows_VehicleApproach を返す`() {
        assertEquals(
            LmuWindowsReadoutListItemType.VehicleApproach,
            ReadoutListItemType.fromId(Simulator.LmuWindows, LmuWindowsReadoutItemKey.VehicleApproach.Root),
        )
    }

    @Test
    fun `lmu_windows の flag は LmuWindows_Flag を返す`() {
        assertEquals(
            LmuWindowsReadoutListItemType.Flag,
            ReadoutListItemType.fromId(Simulator.LmuWindows, LmuWindowsReadoutItemKey.Flag.Root),
        )
    }

    @Test
    fun `lmu_windows の vehicle_damage は LmuWindows_VehicleDamage を返す`() {
        assertEquals(
            LmuWindowsReadoutListItemType.VehicleDamage,
            ReadoutListItemType.fromId(Simulator.LmuWindows, LmuWindowsReadoutItemKey.VehicleDamage.Root),
        )
    }

    @Test
    fun `lmu_windows の my_best_lap は LmuWindows_MyBestLap を返す`() {
        assertEquals(
            LmuWindowsReadoutListItemType.MyBestLap,
            ReadoutListItemType.fromId(Simulator.LmuWindows, LmuWindowsReadoutItemKey.MyBestLap.Root),
        )
    }

    @Test
    fun `gt7_ps5 の best_lap は Gt7Ps5_BestLap を返す`() {
        assertEquals(
            Gt7Ps5ReadoutListItemType.MyBestLap,
            ReadoutListItemType.fromId(Simulator.Gt7Ps5, Gt7Ps5ReadoutItemKey.MyBestLap.Root),
        )
    }

    @Test
    fun `lmu_windows に gt7_ps5 の my_best_lap キーを渡すと null を返す`() {
        assertNull(ReadoutListItemType.fromId(Simulator.LmuWindows, Gt7Ps5ReadoutItemKey.MyBestLap.Root))
    }

    @Test
    fun `gt7_ps5 の remaining_fuel_laps は Gt7Ps5_RemainingFuelLaps を返す`() {
        assertEquals(
            Gt7Ps5ReadoutListItemType.RemainingFuelLaps,
            ReadoutListItemType.fromId(Simulator.Gt7Ps5, Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root),
        )
    }

    @Test
    fun `gt7_ps5 の remaining_fuel は Gt7Ps5_RemainingFuel を返す`() {
        assertEquals(
            Gt7Ps5ReadoutListItemType.RemainingFuel,
            ReadoutListItemType.fromId(Simulator.Gt7Ps5, Gt7Ps5ReadoutItemKey.RemainingFuel.Root),
        )
    }

    @Test
    fun `lmu_windows でシミュレータに属さないキーは null を返す`() {
        assertNull(ReadoutListItemType.fromId(Simulator.LmuWindows, Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root))
    }

    @Test
    fun `lmu_windows の tyre_temperature は TyreTemperature を返す`() {
        assertEquals(
            LmuWindowsReadoutListItemType.TyreTemperature,
            ReadoutListItemType.fromId(Simulator.LmuWindows, LmuWindowsReadoutItemKey.TyreTemperature.Root),
        )
    }

    @Test
    fun `gt7_ps5 でシミュレータに属さないキーは null を返す`() {
        assertNull(ReadoutListItemType.fromId(Simulator.Gt7Ps5, LmuWindowsReadoutItemKey.Flag.Root))
    }

    @Test
    fun `lmu_windows のデフォルト並び順はlistPaneのトップレベル項目のみを含む`() {
        assertEquals(
            listOf(
                LmuWindowsReadoutItemKey.Flag.Root,
                LmuWindowsReadoutItemKey.VehicleApproach.Root,
                LmuWindowsReadoutItemKey.PitTiming.Root,
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                LmuWindowsReadoutItemKey.TyreTemperature.Root,
                LmuWindowsReadoutItemKey.TyreWear.Root,
                LmuWindowsReadoutItemKey.BrakeTemperature.Root,
                LmuWindowsReadoutItemKey.BrakeWear.Root,
                LmuWindowsReadoutItemKey.VehicleDamage.Root,
                LmuWindowsReadoutItemKey.MyBestLap.Root,
            ),
            ReadoutListItemType.defaultOrder(Simulator.LmuWindows),
        )
    }

    @Test
    fun `lmu_windows の brake_wear は LmuWindows_BrakeWear を返す`() {
        assertEquals(
            LmuWindowsReadoutListItemType.BrakeWear,
            ReadoutListItemType.fromId(Simulator.LmuWindows, LmuWindowsReadoutItemKey.BrakeWear.Root),
        )
    }

    @Test
    fun `lmu_windows の brake_temperature は LmuWindows_BrakeTemperature を返す`() {
        assertEquals(
            LmuWindowsReadoutListItemType.BrakeTemperature,
            ReadoutListItemType.fromId(Simulator.LmuWindows, LmuWindowsReadoutItemKey.BrakeTemperature.Root),
        )
    }

    @Test
    fun `lmu_windows の remaining_virtual_energy は LmuWindows_RemainingVirtualEnergy を返す`() {
        assertEquals(
            LmuWindowsReadoutListItemType.RemainingVirtualEnergy,
            ReadoutListItemType.fromId(
                Simulator.LmuWindows,
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
            ),
        )
    }

    @Test
    fun `lmu_windows の tyre_wear は LmuWindows_TyreWear を返す`() {
        assertEquals(
            LmuWindowsReadoutListItemType.TyreWear,
            ReadoutListItemType.fromId(Simulator.LmuWindows, LmuWindowsReadoutItemKey.TyreWear.Root),
        )
    }

    @Test
    fun `lmu_windows の pit_timing は LmuWindows_PitTiming を返す`() {
        assertEquals(
            LmuWindowsReadoutListItemType.PitTiming,
            ReadoutListItemType.fromId(Simulator.LmuWindows, LmuWindowsReadoutItemKey.PitTiming.Root),
        )
    }

    @Test
    fun `gt7_ps5 のデフォルト並び順は2番目に燃料残量を含む`() {
        assertEquals(
            listOf(
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root,
                Gt7Ps5ReadoutItemKey.TyreTemperature.Root,
                Gt7Ps5ReadoutItemKey.MyBestLap.Root,
            ),
            ReadoutListItemType.defaultOrder(Simulator.Gt7Ps5),
        )
    }

    @Test
    fun `gt7_ps5 の tyre_temperature は Gt7Ps5_TyreTemperature を返す`() {
        assertEquals(
            Gt7Ps5ReadoutListItemType.TyreTemperature,
            ReadoutListItemType.fromId(Simulator.Gt7Ps5, Gt7Ps5ReadoutItemKey.TyreTemperature.Root),
        )
    }
}
