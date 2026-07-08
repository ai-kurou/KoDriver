package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutItemKeyTest {

    @Test
    fun `fromValue は一致するキーを返す`() {
        assertEquals(
            ReadoutItemKey.LmuWindows.VehicleApproach,
            ReadoutItemKey.fromValue("lmu_windows_vehicle_approach"),
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
        assertEquals(ReadoutItemKey.LmuWindows.MyBestLap, ReadoutItemKey.fromValue("lmu_windows_my_best_lap"))
        assertEquals(ReadoutItemKey.Gt7Ps5.MyBestLap, ReadoutItemKey.fromValue("gt7_ps5_my_best_lap"))
        assertEquals(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps, ReadoutItemKey.fromValue("gt7_ps5_remaining_fuel_laps"))
    }

    @Test
    fun `fromValue は未知の値のとき null を返す`() {
        assertNull(ReadoutItemKey.fromValue("unknown"))
    }
}
