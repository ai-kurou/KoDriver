package kurou.kodriver.core.designsystem

import org.junit.Test
import kotlin.test.assertEquals

class ListGridKeyboardNavigationTest {
    // 2列: [a b] [c d] [e]
    private val items = listOf("a", "b", "c", "d", "e")

    private fun action(
        direction: ListGridDirection?,
        focused: String?,
    ) = listGridArrowKeyAction(direction, focused, items, columns = 2)

    @Test
    fun `左右キーは同じ行の隣へ移動し行頭と行末では動かない`() {
        assertEquals(ListRowArrowKeyAction.MoveTo(1), action(ListGridDirection.Right, "a"))
        assertEquals(ListRowArrowKeyAction.MoveTo(2), action(ListGridDirection.Left, "d"))
        assertEquals(ListRowArrowKeyAction.Stay, action(ListGridDirection.Left, "a"))
        assertEquals(ListRowArrowKeyAction.Stay, action(ListGridDirection.Left, "c"))
        assertEquals(ListRowArrowKeyAction.Stay, action(ListGridDirection.Right, "b"))
        assertEquals(ListRowArrowKeyAction.Stay, action(ListGridDirection.Right, "e"))
    }

    @Test
    fun `上下キーは同じ列の上下へ移動し先頭行の上と最終行の下では動かない`() {
        assertEquals(ListRowArrowKeyAction.MoveTo(2), action(ListGridDirection.Down, "a"))
        assertEquals(ListRowArrowKeyAction.MoveTo(3), action(ListGridDirection.Down, "b"))
        assertEquals(ListRowArrowKeyAction.MoveTo(0), action(ListGridDirection.Up, "c"))
        assertEquals(ListRowArrowKeyAction.Stay, action(ListGridDirection.Up, "a"))
        assertEquals(ListRowArrowKeyAction.Stay, action(ListGridDirection.Up, "b"))
        assertEquals(ListRowArrowKeyAction.Stay, action(ListGridDirection.Down, "e"))
    }

    @Test
    fun `最終行が欠けていて真下がないときは下キーで最後の項目へ移動する`() {
        // d の真下はないが、最終行 [e] があるため e へ移る。
        assertEquals(ListRowArrowKeyAction.MoveTo(4), action(ListGridDirection.Down, "d"))
        assertEquals(ListRowArrowKeyAction.MoveTo(4), action(ListGridDirection.Down, "c"))
    }

    @Test
    fun `方向がない場合と項目の外にフォーカスがある場合は無視する`() {
        assertEquals(ListRowArrowKeyAction.Ignore, action(null, "a"))
        assertEquals(ListRowArrowKeyAction.Ignore, action(ListGridDirection.Down, null))
        assertEquals(ListRowArrowKeyAction.Ignore, action(ListGridDirection.Down, "unknown"))
    }
}
