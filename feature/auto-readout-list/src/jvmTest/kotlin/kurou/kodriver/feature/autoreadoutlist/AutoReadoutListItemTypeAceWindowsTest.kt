package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AutoReadoutListItemTypeAceWindowsTest {
    @Test
    fun `ace_windows の remaining_fuel は AceWindows_RemainingFuel を返す`() {
        assertEquals(
            AceWindowsAutoReadoutListItemType.RemainingFuel,
            AutoReadoutListItemType.fromId(Simulator.AceWindows, AceWindowsReadoutItemKey.RemainingFuel.Root),
        )
    }

    @Test
    fun `ace_windows の flag は AceWindows_Flag を返す`() {
        assertEquals(
            AceWindowsAutoReadoutListItemType.Flag,
            AutoReadoutListItemType.fromId(Simulator.AceWindows, AceWindowsReadoutItemKey.Flag.Root),
        )
    }

    @Test
    fun `ace_windows の my_best_lap は AceWindows_MyBestLap を返す`() {
        assertEquals(
            AceWindowsAutoReadoutListItemType.MyBestLap,
            AutoReadoutListItemType.fromId(Simulator.AceWindows, AceWindowsReadoutItemKey.MyBestLap.Root),
        )
    }

    @Test
    fun `ace_windows の remaining_fuel_laps は AceWindows_RemainingFuelLaps を返す`() {
        assertEquals(
            AceWindowsAutoReadoutListItemType.RemainingFuelLaps,
            AutoReadoutListItemType.fromId(Simulator.AceWindows, AceWindowsReadoutItemKey.RemainingFuelLaps.Root),
        )
    }

    @Test
    fun `ace_windows でシミュレーターに属さないキーは null を返す`() {
        assertNull(AutoReadoutListItemType.fromId(Simulator.AceWindows, Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root))
    }

    @Test
    fun `ace_windows の tyre_temperature は AceWindows_TyreTemperature を返す`() {
        assertEquals(
            AceWindowsAutoReadoutListItemType.TyreTemperature,
            AutoReadoutListItemType.fromId(Simulator.AceWindows, AceWindowsReadoutItemKey.TyreTemperature.Root),
        )
    }

    @Test
    fun `ace_windows の vehicle_approach は AceWindows_VehicleApproach を返す`() {
        assertEquals(
            AceWindowsAutoReadoutListItemType.VehicleApproach,
            AutoReadoutListItemType.fromId(Simulator.AceWindows, AceWindowsReadoutItemKey.VehicleApproach.Root),
        )
    }

    @Test
    fun `ace_windows のデフォルト並び順はフラッグ・車両接近・燃料残り周回数・燃料残量・タイヤ温度・自己ベストラップの順`() {
        assertEquals(
            listOf(
                AceWindowsReadoutItemKey.Flag.Root,
                AceWindowsReadoutItemKey.VehicleApproach.Root,
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
                AceWindowsReadoutItemKey.RemainingFuel.Root,
                AceWindowsReadoutItemKey.TyreTemperature.Root,
                AceWindowsReadoutItemKey.MyBestLap.Root,
            ),
            AutoReadoutListItemType.defaultOrder(Simulator.AceWindows),
        )
    }
}
