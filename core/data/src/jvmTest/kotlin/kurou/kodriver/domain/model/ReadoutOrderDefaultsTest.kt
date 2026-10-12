package kurou.kodriver.domain.model

import kurou.kodriver.data.APP_READOUT_CATALOG
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutOrderDefaultsTest {
    @Test
    fun `LMUのデフォルト順序はフラッグが先頭で自己ベストが末尾`() {
        val order = APP_READOUT_CATALOG.defaultReadoutOrder(Simulator.LmuWindows)

        assertEquals(LmuWindowsReadoutItemKey.Flag.Root, order.first())
        assertEquals(LmuWindowsReadoutItemKey.MyBestLap.Root, order.last())
        assertEquals(
            listOf(
                LmuWindowsReadoutItemKey.BrakeTemperature.Root,
                LmuWindowsReadoutItemKey.BrakeWear.Root,
                LmuWindowsReadoutItemKey.VehicleDamage.Root,
                LmuWindowsReadoutItemKey.MyBestLap.Root,
            ),
            order.drop(6),
        )
        assertEquals(APP_READOUT_CATALOG.entries.filterIsInstance<LmuWindowsReadoutItemKey.TopLevel>().size, order.size)
    }

    @Test
    fun `GT7のデフォルト順序は残り燃料周回数が先頭で自己ベストが末尾`() {
        assertEquals(
            listOf(
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root,
                Gt7Ps5ReadoutItemKey.TyreTemperature.Root,
                Gt7Ps5ReadoutItemKey.MyBestLap.Root,
            ),
            APP_READOUT_CATALOG.defaultReadoutOrder(Simulator.Gt7Ps5),
        )
    }

    @Test
    fun `ACEのデフォルト順序は安全通知に続いて燃料とタイヤ温度を優先する`() {
        assertEquals(
            listOf(
                AceWindowsReadoutItemKey.Flag.Root,
                AceWindowsReadoutItemKey.VehicleApproach.Root,
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
                AceWindowsReadoutItemKey.RemainingFuel.Root,
                AceWindowsReadoutItemKey.TyreTemperature.Root,
                AceWindowsReadoutItemKey.MyBestLap.Root,
            ),
            APP_READOUT_CATALOG.defaultReadoutOrder(Simulator.AceWindows),
        )
    }
}
