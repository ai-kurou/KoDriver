package kurou.kodriver.domain.model

import kurou.kodriver.data.APP_READOUT_CATALOG
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutItemKeyTest {
    @Test
    fun `LMUのentriesは全キーを既存の順序で網羅する`() {
        assertEquals(
            listOf(
                LmuWindowsReadoutItemKey.VehicleApproach.Root,
                LmuWindowsReadoutItemKey.VehicleApproach.Sustained,
                LmuWindowsReadoutItemKey.VehicleApproach.StartReadout,
                LmuWindowsReadoutItemKey.Flag.Root,
                LmuWindowsReadoutItemKey.Flag.BlueFlag,
                LmuWindowsReadoutItemKey.Flag.SectorYellowFlag,
                LmuWindowsReadoutItemKey.Flag.FullCourseYellow,
                LmuWindowsReadoutItemKey.Flag.RedFlag,
                LmuWindowsReadoutItemKey.VehicleDamage.Root,
                LmuWindowsReadoutItemKey.VehicleDamage.Overheat,
                LmuWindowsReadoutItemKey.VehicleDamage.PartDetached,
                LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached,
                LmuWindowsReadoutItemKey.TyreTemperature.Root,
                LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning,
                LmuWindowsReadoutItemKey.TyreTemperature.LowWarning,
                LmuWindowsReadoutItemKey.PitTiming.Root,
                LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy,
                LmuWindowsReadoutItemKey.PitTiming.TyreWear,
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.WarningReadout,
                LmuWindowsReadoutItemKey.TyreWear.Root,
                LmuWindowsReadoutItemKey.TyreWear.WarningReadout,
                LmuWindowsReadoutItemKey.BrakeTemperature.Root,
                LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout,
                LmuWindowsReadoutItemKey.BrakeWear.Root,
                LmuWindowsReadoutItemKey.BrakeWear.WarningReadout,
                LmuWindowsReadoutItemKey.MyBestLap.Root,
                LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled,
            ),
            LmuWindowsReadoutItemKey.entries,
        )
    }

    @Test
    fun `GT7のentriesは全キーを既存の順序で網羅する`() {
        assertEquals(
            listOf(
                Gt7Ps5ReadoutItemKey.MyBestLap.Root,
                Gt7Ps5ReadoutItemKey.MyBestLap.DetailEnabled,
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.DetailEnabled,
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root,
                Gt7Ps5ReadoutItemKey.RemainingFuel.DetailEnabled,
                Gt7Ps5ReadoutItemKey.TyreTemperature.Root,
                Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning,
            ),
            Gt7Ps5ReadoutItemKey.entries,
        )
    }

    @Test
    fun `ACEのentriesは全キーを既存の順序で網羅する`() {
        assertEquals(
            listOf(
                AceWindowsReadoutItemKey.VehicleApproach.Root,
                AceWindowsReadoutItemKey.VehicleApproach.StartReadout,
                AceWindowsReadoutItemKey.Flag.Root,
                AceWindowsReadoutItemKey.Flag.WhiteFlag,
                AceWindowsReadoutItemKey.Flag.GreenFlag,
                AceWindowsReadoutItemKey.Flag.RedFlag,
                AceWindowsReadoutItemKey.Flag.BlueFlag,
                AceWindowsReadoutItemKey.Flag.YellowFlag,
                AceWindowsReadoutItemKey.Flag.BlackFlag,
                AceWindowsReadoutItemKey.Flag.BlackWhiteFlag,
                AceWindowsReadoutItemKey.Flag.CheckeredFlag,
                AceWindowsReadoutItemKey.Flag.OrangeCircleFlag,
                AceWindowsReadoutItemKey.Flag.RedYellowStripesFlag,
                AceWindowsReadoutItemKey.RemainingFuel.Root,
                AceWindowsReadoutItemKey.RemainingFuel.DetailEnabled,
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
                AceWindowsReadoutItemKey.RemainingFuelLaps.DetailEnabled,
                AceWindowsReadoutItemKey.TyreTemperature.Root,
                AceWindowsReadoutItemKey.TyreTemperature.OverheatWarning,
                AceWindowsReadoutItemKey.MyBestLap.Root,
                AceWindowsReadoutItemKey.MyBestLap.DetailEnabled,
            ),
            AceWindowsReadoutItemKey.entries,
        )
    }

    @Test
    fun `entriesはLMUとGT7とACEのentriesを順に連結した一覧と一致する`() {
        assertEquals(
            LmuWindowsReadoutItemKey.entries + Gt7Ps5ReadoutItemKey.entries + AceWindowsReadoutItemKey.entries,
            APP_READOUT_CATALOG.entries,
        )
    }

    @Test
    fun `entriesのvalueに重複がない`() {
        val values = APP_READOUT_CATALOG.entries.map { it.value }
        assertEquals(values.size, values.toSet().size)
    }

    @Test
    fun `fromValueは全entriesのキーを復元する`() {
        APP_READOUT_CATALOG.entries.forEach { key ->
            assertEquals(key, APP_READOUT_CATALOG.fromValue(key.value))
        }
    }

    @Test
    fun `fromValue はLMUとGT7のキーに一致するキーを返す`() {
        assertEquals(
            LmuWindowsReadoutItemKey.VehicleApproach.Root,
            APP_READOUT_CATALOG.fromValue("lmu_windows_vehicle_approach"),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.VehicleApproach.Sustained,
            APP_READOUT_CATALOG.fromValue("lmu_windows_vehicle_approach_sustained"),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.VehicleApproach.StartReadout,
            APP_READOUT_CATALOG.fromValue("lmu_windows_vehicle_approach_start_readout"),
        )
        assertEquals(LmuWindowsReadoutItemKey.Flag.Root, APP_READOUT_CATALOG.fromValue("lmu_windows_flag"))
        assertEquals(
            LmuWindowsReadoutItemKey.VehicleDamage.Root,
            APP_READOUT_CATALOG.fromValue("lmu_windows_vehicle_damage"),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.TyreTemperature.Root,
            APP_READOUT_CATALOG.fromValue("lmu_windows_tyre_temperature"),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning,
            APP_READOUT_CATALOG.fromValue("lmu_windows_tyre_temperature_overheat_warning"),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.TyreTemperature.LowWarning,
            APP_READOUT_CATALOG.fromValue("lmu_windows_tyre_temperature_low_warning"),
        )
        assertEquals(LmuWindowsReadoutItemKey.MyBestLap.Root, APP_READOUT_CATALOG.fromValue("lmu_windows_my_best_lap"))
        assertEquals(
            LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled,
            APP_READOUT_CATALOG.fromValue("lmu_windows_my_best_lap_detail_enabled"),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
            APP_READOUT_CATALOG.fromValue("lmu_windows_remaining_virtual_energy"),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.RemainingVirtualEnergy.WarningReadout,
            APP_READOUT_CATALOG.fromValue("lmu_windows_remaining_virtual_energy_warning_readout"),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.TyreWear.Root,
            APP_READOUT_CATALOG.fromValue("lmu_windows_tyre_wear"),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.TyreWear.WarningReadout,
            APP_READOUT_CATALOG.fromValue("lmu_windows_tyre_wear_warning_readout"),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.PitTiming.Root,
            APP_READOUT_CATALOG.fromValue("lmu_windows_pit_timing"),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.BrakeTemperature.Root,
            APP_READOUT_CATALOG.fromValue("lmu_windows_brake_temperature"),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout,
            APP_READOUT_CATALOG.fromValue("lmu_windows_brake_temperature_warning_readout"),
        )
        assertEquals(LmuWindowsReadoutItemKey.BrakeWear.Root, APP_READOUT_CATALOG.fromValue("lmu_windows_brake_wear"))
        assertEquals(Gt7Ps5ReadoutItemKey.MyBestLap.Root, APP_READOUT_CATALOG.fromValue("gt7_ps5_my_best_lap"))
        assertEquals(
            Gt7Ps5ReadoutItemKey.MyBestLap.DetailEnabled,
            APP_READOUT_CATALOG.fromValue("gt7_ps5_my_best_lap_detail_enabled"),
        )
        assertEquals(
            Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
            APP_READOUT_CATALOG.fromValue("gt7_ps5_remaining_fuel_laps"),
        )
        assertEquals(
            Gt7Ps5ReadoutItemKey.RemainingFuelLaps.DetailEnabled,
            APP_READOUT_CATALOG.fromValue("gt7_ps5_remaining_fuel_laps_detail_enabled"),
        )
        assertEquals(
            Gt7Ps5ReadoutItemKey.RemainingFuel.Root,
            APP_READOUT_CATALOG.fromValue("gt7_ps5_remaining_fuel"),
        )
        assertEquals(
            Gt7Ps5ReadoutItemKey.RemainingFuel.DetailEnabled,
            APP_READOUT_CATALOG.fromValue("gt7_ps5_remaining_fuel_detail_enabled"),
        )
        assertEquals(
            Gt7Ps5ReadoutItemKey.TyreTemperature.Root,
            APP_READOUT_CATALOG.fromValue("gt7_ps5_tyre_temperature"),
        )
    }

    @Test
    fun `fromValue はACEのキーに一致するキーを返す`() {
        assertEquals(
            AceWindowsReadoutItemKey.RemainingFuel.Root,
            APP_READOUT_CATALOG.fromValue("ace_windows_remaining_fuel"),
        )
        assertEquals(
            AceWindowsReadoutItemKey.RemainingFuel.DetailEnabled,
            APP_READOUT_CATALOG.fromValue("ace_windows_remaining_fuel_detail_enabled"),
        )
        assertEquals(
            AceWindowsReadoutItemKey.Flag.Root,
            APP_READOUT_CATALOG.fromValue("ace_windows_flag"),
        )
        assertEquals(AceWindowsReadoutItemKey.Flag.WhiteFlag, APP_READOUT_CATALOG.fromValue("ace_windows_white_flag"))
        assertEquals(AceWindowsReadoutItemKey.Flag.GreenFlag, APP_READOUT_CATALOG.fromValue("ace_windows_green_flag"))
        assertEquals(AceWindowsReadoutItemKey.Flag.RedFlag, APP_READOUT_CATALOG.fromValue("ace_windows_red_flag"))
        assertEquals(AceWindowsReadoutItemKey.Flag.BlueFlag, APP_READOUT_CATALOG.fromValue("ace_windows_blue_flag"))
        assertEquals(AceWindowsReadoutItemKey.Flag.YellowFlag, APP_READOUT_CATALOG.fromValue("ace_windows_yellow_flag"))
        assertEquals(AceWindowsReadoutItemKey.Flag.BlackFlag, APP_READOUT_CATALOG.fromValue("ace_windows_black_flag"))
        assertEquals(
            AceWindowsReadoutItemKey.Flag.BlackWhiteFlag,
            APP_READOUT_CATALOG.fromValue("ace_windows_black_white_flag"),
        )
        assertEquals(
            AceWindowsReadoutItemKey.Flag.CheckeredFlag,
            APP_READOUT_CATALOG.fromValue("ace_windows_checkered_flag"),
        )
        assertEquals(
            AceWindowsReadoutItemKey.Flag.OrangeCircleFlag,
            APP_READOUT_CATALOG.fromValue("ace_windows_orange_circle_flag"),
        )
        assertEquals(
            AceWindowsReadoutItemKey.Flag.RedYellowStripesFlag,
            APP_READOUT_CATALOG.fromValue("ace_windows_red_yellow_stripes_flag"),
        )
        assertEquals(
            AceWindowsReadoutItemKey.TyreTemperature.Root,
            APP_READOUT_CATALOG.fromValue("ace_windows_tyre_temperature"),
        )
        assertEquals(
            AceWindowsReadoutItemKey.VehicleApproach.Root,
            APP_READOUT_CATALOG.fromValue("ace_windows_vehicle_approach"),
        )
        assertEquals(
            AceWindowsReadoutItemKey.VehicleApproach.StartReadout,
            APP_READOUT_CATALOG.fromValue("ace_windows_vehicle_approach_start_readout"),
        )
        assertEquals(
            AceWindowsReadoutItemKey.MyBestLap.Root,
            APP_READOUT_CATALOG.fromValue("ace_windows_my_best_lap"),
        )
        assertEquals(
            AceWindowsReadoutItemKey.MyBestLap.DetailEnabled,
            APP_READOUT_CATALOG.fromValue("ace_windows_my_best_lap_detail_enabled"),
        )
        assertEquals(
            AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
            APP_READOUT_CATALOG.fromValue("ace_windows_remaining_fuel_laps"),
        )
        assertEquals(
            AceWindowsReadoutItemKey.RemainingFuelLaps.DetailEnabled,
            APP_READOUT_CATALOG.fromValue("ace_windows_remaining_fuel_laps_detail_enabled"),
        )
    }

    @Test
    fun `fromValue は未知の値のとき null を返す`() {
        assertNull(APP_READOUT_CATALOG.fromValue("unknown"))
    }

    @Test
    fun `車両接近の Root のみ supportsQueue が false`() {
        assertEquals(false, LmuWindowsReadoutItemKey.VehicleApproach.Root.supportsQueue)
        assertEquals(false, AceWindowsReadoutItemKey.VehicleApproach.Root.supportsQueue)
    }

    @Test
    fun `車両接近以外の Root は supportsQueue が true`() {
        assertEquals(true, LmuWindowsReadoutItemKey.Flag.Root.supportsQueue)
        assertEquals(true, LmuWindowsReadoutItemKey.VehicleDamage.Root.supportsQueue)
        assertEquals(true, LmuWindowsReadoutItemKey.TyreTemperature.Root.supportsQueue)
        assertEquals(true, LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.supportsQueue)
        assertEquals(true, LmuWindowsReadoutItemKey.TyreWear.Root.supportsQueue)
        assertEquals(true, LmuWindowsReadoutItemKey.BrakeWear.Root.supportsQueue)
        assertEquals(true, LmuWindowsReadoutItemKey.PitTiming.Root.supportsQueue)
        assertEquals(true, LmuWindowsReadoutItemKey.MyBestLap.Root.supportsQueue)
        assertEquals(true, Gt7Ps5ReadoutItemKey.MyBestLap.Root.supportsQueue)
        assertEquals(true, Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.supportsQueue)
        assertEquals(true, Gt7Ps5ReadoutItemKey.RemainingFuel.Root.supportsQueue)
        assertEquals(true, Gt7Ps5ReadoutItemKey.TyreTemperature.Root.supportsQueue)
        assertEquals(true, AceWindowsReadoutItemKey.RemainingFuel.Root.supportsQueue)
        assertEquals(true, AceWindowsReadoutItemKey.Flag.Root.supportsQueue)
        assertEquals(true, AceWindowsReadoutItemKey.TyreTemperature.Root.supportsQueue)
        assertEquals(true, AceWindowsReadoutItemKey.MyBestLap.Root.supportsQueue)
        assertEquals(true, AceWindowsReadoutItemKey.RemainingFuelLaps.Root.supportsQueue)
    }
}
