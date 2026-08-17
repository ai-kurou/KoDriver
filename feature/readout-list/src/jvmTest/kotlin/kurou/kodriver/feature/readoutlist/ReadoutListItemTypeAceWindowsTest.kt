package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadoutListItemTypeAceWindowsTest {
    @Test
    fun `ace_windows の remaining_fuel は AceWindows_RemainingFuel を返す`() {
        assertEquals(
            ReadoutListItemType.AceWindows.RemainingFuel,
            ReadoutListItemType.fromId(Simulator.AceWindows, ReadoutItemKey.AceWindows.RemainingFuel.Root),
        )
    }

    @Test
    fun `ace_windows の flag は AceWindows_Flag を返す`() {
        assertEquals(
            ReadoutListItemType.AceWindows.Flag,
            ReadoutListItemType.fromId(Simulator.AceWindows, ReadoutItemKey.AceWindows.Flag.Root),
        )
    }

    @Test
    fun `ace_windows でシミュレータに属さないキーは null を返す`() {
        assertNull(ReadoutListItemType.fromId(Simulator.AceWindows, ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root))
    }

    @Test
    fun `ace_windows の tyre_temperature は detailPane未実装のため null を返す`() {
        assertNull(ReadoutListItemType.fromId(Simulator.AceWindows, ReadoutItemKey.AceWindows.TyreTemperature.Root))
    }

    @Test
    fun `ace_windows のデフォルト並び順はフラッグ・タイヤ温度・燃料残量の順`() {
        assertEquals(
            listOf(
                ReadoutItemKey.AceWindows.Flag.Root,
                ReadoutItemKey.AceWindows.TyreTemperature.Root,
                ReadoutItemKey.AceWindows.RemainingFuel.Root,
            ),
            ReadoutListItemType.defaultOrder(Simulator.AceWindows),
        )
    }
}
