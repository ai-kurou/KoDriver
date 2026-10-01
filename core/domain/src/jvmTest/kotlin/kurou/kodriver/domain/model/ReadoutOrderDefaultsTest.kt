package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutOrderDefaultsTest {
    @Test
    fun `LMUのデフォルト順序はフラッグが先頭で自己ベストが末尾`() {
        val order = defaultReadoutOrder(Simulator.LmuWindows)

        assertEquals(ReadoutItemKey.LmuWindows.Flag.Root, order.first())
        assertEquals(ReadoutItemKey.LmuWindows.MyBestLap.Root, order.last())
        assertEquals(ReadoutItemKey.entries.filterIsInstance<ReadoutItemKey.LmuWindows.TopLevel>().size, order.size)
    }

    @Test
    fun `GT7のデフォルト順序は残り燃料周回数が先頭で自己ベストが末尾`() {
        assertEquals(
            listOf(
                ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
                ReadoutItemKey.Gt7Ps5.RemainingFuel.Root,
                ReadoutItemKey.Gt7Ps5.TyreTemperature.Root,
                ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
            ),
            defaultReadoutOrder(Simulator.Gt7Ps5),
        )
    }

    @Test
    fun `ACEのデフォルト順序はフラッグが先頭で自己ベストが末尾`() {
        val order = defaultReadoutOrder(Simulator.AceWindows)

        assertEquals(ReadoutItemKey.AceWindows.Flag.Root, order.first())
        assertEquals(ReadoutItemKey.AceWindows.MyBestLap.Root, order.last())
    }
}
