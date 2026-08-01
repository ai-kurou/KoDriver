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
    fun `ace_windows のデフォルト並び順は先頭がフラッグ`() {
        assertEquals(
            listOf(
                ReadoutItemKey.AceWindows.Flag.Root,
                ReadoutItemKey.AceWindows.RemainingFuel.Root,
            ),
            ReadoutListItemType.defaultOrder(Simulator.AceWindows),
        )
    }
}
