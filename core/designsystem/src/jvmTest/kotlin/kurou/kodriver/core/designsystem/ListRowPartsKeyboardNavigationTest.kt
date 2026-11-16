package kurou.kodriver.core.designsystem

import androidx.compose.ui.focus.FocusRequester
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ListRowPartsKeyboardNavigationTest {
    private val allEnabled = listOf(true, true, true, true)

    @Test
    fun `右は次の部品、左は前の部品へ移動する`() {
        assertEquals(ListRowArrowKeyAction.MoveTo(2), listRowPartArrowKeyAction(1, 1, allEnabled))
        assertEquals(ListRowArrowKeyAction.MoveTo(0), listRowPartArrowKeyAction(-1, 1, allEnabled))
    }

    @Test
    fun `端では動かずキーだけ消費する`() {
        assertEquals(ListRowArrowKeyAction.Stay, listRowPartArrowKeyAction(-1, 0, allEnabled))
        assertEquals(ListRowArrowKeyAction.Stay, listRowPartArrowKeyAction(1, 3, allEnabled))
    }

    @Test
    fun `無効な部品は飛ばし、先に有効な部品がなければ動かない`() {
        val enabled = listOf(true, true, false, true)
        assertEquals(ListRowArrowKeyAction.MoveTo(3), listRowPartArrowKeyAction(1, 1, enabled))
        assertEquals(ListRowArrowKeyAction.MoveTo(1), listRowPartArrowKeyAction(-1, 3, enabled))
        assertEquals(ListRowArrowKeyAction.Stay, listRowPartArrowKeyAction(1, 1, listOf(true, true, false, false)))
    }

    @Test
    fun `対象外のキーや行内の部品にフォーカスがないときは処理しない`() {
        assertEquals(ListRowArrowKeyAction.Ignore, listRowPartArrowKeyAction(null, 1, allEnabled))
        assertEquals(ListRowArrowKeyAction.Ignore, listRowPartArrowKeyAction(1, null, allEnabled))
        assertEquals(ListRowArrowKeyAction.Ignore, listRowPartArrowKeyAction(1, 9, allEnabled))
    }

    @Test
    fun `フォーカス中の部品は入ったときに記録され、外れたときは自分の記録だけ消える`() {
        val state = ListRowPartsFocusState(partCount = 3, firstRequester = FocusRequester())
        assertNull(state.focusedPart)

        state.onPartFocusChanged(1, true)
        assertEquals(1, state.focusedPart)

        state.onPartFocusChanged(2, true)
        state.onPartFocusChanged(1, false)
        assertEquals(2, state.focusedPart)

        state.onPartFocusChanged(2, false)
        assertNull(state.focusedPart)
    }

    @Test
    fun `最初の部品は渡した FocusRequester を使う`() {
        val first = FocusRequester()
        val state = ListRowPartsFocusState(partCount = 2, firstRequester = first)
        assertEquals(first, state.requesterFor(0))
    }
}
