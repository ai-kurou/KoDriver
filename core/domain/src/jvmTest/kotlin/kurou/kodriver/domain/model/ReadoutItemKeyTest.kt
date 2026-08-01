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
            ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root,
            ReadoutItemKey.fromValue("lmu_windows_remaining_virtual_energy"),
        )
        assertEquals(
            ReadoutItemKey.LmuWindows.TyreWear.Root,
            ReadoutItemKey.fromValue("lmu_windows_tyre_wear"),
        )
        assertEquals(
            ReadoutItemKey.LmuWindows.PitTiming.Root,
            ReadoutItemKey.fromValue("lmu_windows_pit_timing"),
        )
        assertEquals(ReadoutItemKey.Gt7Ps5.MyBestLap.Root, ReadoutItemKey.fromValue("gt7_ps5_my_best_lap"))
        assertEquals(
            ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
            ReadoutItemKey.fromValue("gt7_ps5_remaining_fuel_laps"),
        )
        assertEquals(
            ReadoutItemKey.Gt7Ps5.RemainingFuel.Root,
            ReadoutItemKey.fromValue("gt7_ps5_remaining_fuel"),
        )
        assertEquals(
            ReadoutItemKey.AceWindows.RemainingFuel.Root,
            ReadoutItemKey.fromValue("ace_windows_remaining_fuel"),
        )
        assertEquals(
            ReadoutItemKey.AceWindows.Flag.Root,
            ReadoutItemKey.fromValue("ace_windows_flag"),
        )
        assertEquals(ReadoutItemKey.AceWindows.Flag.WhiteFlag, ReadoutItemKey.fromValue("ace_windows_white_flag"))
        assertEquals(ReadoutItemKey.AceWindows.Flag.GreenFlag, ReadoutItemKey.fromValue("ace_windows_green_flag"))
        assertEquals(ReadoutItemKey.AceWindows.Flag.RedFlag, ReadoutItemKey.fromValue("ace_windows_red_flag"))
        assertEquals(ReadoutItemKey.AceWindows.Flag.BlueFlag, ReadoutItemKey.fromValue("ace_windows_blue_flag"))
        assertEquals(ReadoutItemKey.AceWindows.Flag.YellowFlag, ReadoutItemKey.fromValue("ace_windows_yellow_flag"))
        assertEquals(ReadoutItemKey.AceWindows.Flag.BlackFlag, ReadoutItemKey.fromValue("ace_windows_black_flag"))
        assertEquals(
            ReadoutItemKey.AceWindows.Flag.BlackWhiteFlag,
            ReadoutItemKey.fromValue("ace_windows_black_white_flag"),
        )
        assertEquals(
            ReadoutItemKey.AceWindows.Flag.CheckeredFlag,
            ReadoutItemKey.fromValue("ace_windows_checkered_flag"),
        )
        assertEquals(
            ReadoutItemKey.AceWindows.Flag.OrangeCircleFlag,
            ReadoutItemKey.fromValue("ace_windows_orange_circle_flag"),
        )
        assertEquals(
            ReadoutItemKey.AceWindows.Flag.RedYellowStripesFlag,
            ReadoutItemKey.fromValue("ace_windows_red_yellow_stripes_flag"),
        )
    }

    @Test
    fun `fromValue は未知の値のとき null を返す`() {
        assertNull(ReadoutItemKey.fromValue("unknown"))
    }

    @Test
    fun `車両接近の Root のみ supportsQueue が false`() {
        assertEquals(false, ReadoutItemKey.LmuWindows.VehicleApproach.Root.supportsQueue)
    }

    @Test
    fun `車両接近以外の Root は supportsQueue が true`() {
        assertEquals(true, ReadoutItemKey.LmuWindows.Flag.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.VehicleDamage.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.TyreTemperature.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.TyreWear.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.PitTiming.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.LmuWindows.MyBestLap.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.Gt7Ps5.MyBestLap.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.Gt7Ps5.RemainingFuel.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.AceWindows.RemainingFuel.Root.supportsQueue)
        assertEquals(true, ReadoutItemKey.AceWindows.Flag.Root.supportsQueue)
    }
}
