package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutItemKeyTest {

    @Test
    fun `fromValue は一致するキーを返す`() {
        assertEquals(
            ReadoutItemKey.LmuWindows.VehicleApproach.Root,
            ReadoutItemKey.fromValue("lmu_windows_vehicle_approach"),
        )
        assertEquals(
            ReadoutItemKey.LmuWindows.VehicleApproach.Sustained,
            ReadoutItemKey.fromValue("lmu_windows_vehicle_approach_sustained"),
        )
        assertEquals(
            ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout,
            ReadoutItemKey.fromValue("lmu_windows_vehicle_approach_start_readout"),
        )
        assertEquals(ReadoutItemKey.LmuWindows.Flag.Root, ReadoutItemKey.fromValue("lmu_windows_flag"))
        assertEquals(
            ReadoutItemKey.LmuWindows.VehicleDamage.Root,
            ReadoutItemKey.fromValue("lmu_windows_vehicle_damage"),
        )
        assertEquals(
            ReadoutItemKey.LmuWindows.TyreTemperature.Root,
            ReadoutItemKey.fromValue("lmu_windows_tyre_temperature"),
        )
        assertEquals(
            ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning,
            ReadoutItemKey.fromValue("lmu_windows_tyre_temperature_overheat_warning"),
        )
        assertEquals(
            ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning,
            ReadoutItemKey.fromValue("lmu_windows_tyre_temperature_low_warning"),
        )
        assertEquals(ReadoutItemKey.LmuWindows.MyBestLap.Root, ReadoutItemKey.fromValue("lmu_windows_my_best_lap"))
        assertEquals(
            ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root,
            ReadoutItemKey.fromValue("lmu_windows_remaining_virtual_energy_laps"),
        )
        assertEquals(ReadoutItemKey.Gt7Ps5.MyBestLap.Root, ReadoutItemKey.fromValue("gt7_ps5_my_best_lap"))
        assertEquals(
            ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
            ReadoutItemKey.fromValue("gt7_ps5_remaining_fuel_laps"),
        )
    }

    @Test
    fun `fromValue は未知の値のとき null を返す`() {
        assertNull(ReadoutItemKey.fromValue("unknown"))
    }

    @Test
    fun `車両接近関連のキーは supportsQueue が false`() {
        assertEquals(false, ReadoutItemKey.LmuWindows.VehicleApproach.Root.supportsQueue)
        assertEquals(false, ReadoutItemKey.LmuWindows.VehicleApproach.Sustained.supportsQueue)
        assertEquals(false, ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout.supportsQueue)
    }

    @Test
    fun `車両接近以外のキーは supportsQueue が true`() {
        assertEquals(true, ReadoutItemKey.LmuWindows.Flag.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.Flag.BlueFlag.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.Flag.FullCourseYellow.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.Flag.RedFlag.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.VehicleDamage.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.VehicleDamage.Overheat.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.TyreTemperature.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.MyBestLap.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.Gt7Ps5.MyBestLap.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root.supportsQueue)
    }
}
