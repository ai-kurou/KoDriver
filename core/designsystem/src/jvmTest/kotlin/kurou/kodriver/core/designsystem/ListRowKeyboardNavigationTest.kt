package kurou.kodriver.core.designsystem

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ListRowKeyboardNavigationTest {
    private val items = listOf("flag", "damage", "fuel")

    @Test
    fun `処理方針は範囲内なら移動先を返し端では動かさず行外では無視する`() {
        assertEquals(
            ListRowArrowKeyAction.MoveTo(1),
            listRowArrowKeyAction(step = 1, focusedItemKey = "flag", itemKeys = items),
        )
        assertEquals(
            ListRowArrowKeyAction.MoveTo(1),
            listRowArrowKeyAction(step = -1, focusedItemKey = "fuel", itemKeys = items),
        )
        assertEquals(
            ListRowArrowKeyAction.Stay,
            listRowArrowKeyAction(step = -1, focusedItemKey = "flag", itemKeys = items),
        )
        assertEquals(
            ListRowArrowKeyAction.Stay,
            listRowArrowKeyAction(step = 1, focusedItemKey = "fuel", itemKeys = items),
        )
        assertEquals(
            ListRowArrowKeyAction.Ignore,
            listRowArrowKeyAction(step = 1, focusedItemKey = null, itemKeys = items),
        )
        assertEquals(
            ListRowArrowKeyAction.Ignore,
            listRowArrowKeyAction(step = 1, focusedItemKey = "unknown", itemKeys = items),
        )
        assertEquals(
            ListRowArrowKeyAction.Ignore,
            listRowArrowKeyAction(step = null, focusedItemKey = "flag", itemKeys = items),
        )
    }

    @Test
    fun `行のフォーカス状態は自分の行が外れたときだけ解除される`() {
        val state = ListRowFocusState()
        state.onRowFocusChanged(itemKey = "b", hasFocus = true)
        state.onRowFocusChanged(itemKey = "a", hasFocus = false)
        assertEquals("b", state.focusedItemKey)

        state.onRowFocusChanged(itemKey = "b", hasFocus = false)
        assertNull(state.focusedItemKey)
    }

    @Test
    fun `並べ替えは同じ並びのままでは重ねて依頼されず並びが変わると再び依頼できる`() {
        val state = ListRowFocusState()
        val reordered = listOf("damage", "flag", "fuel")

        assertTrue(state.tryStartReorder(items))
        assertFalse(state.tryStartReorder(items))
        assertTrue(state.tryStartReorder(reordered))
        assertFalse(state.tryStartReorder(reordered))
        assertTrue(state.tryStartReorder(items))
    }

    @Test
    fun `並びが変わったと通知されると同じ並びに戻っても再び並べ替えを依頼できる`() {
        val state = ListRowFocusState()

        assertTrue(state.tryStartReorder(items))
        state.onItemKeysChanged()

        assertTrue(state.tryStartReorder(items))
    }

    @Test
    fun `同じ項目キーには同じ FocusRequester を返す`() {
        val state = ListRowFocusState()

        assertEquals(state.requesterFor("flag"), state.requesterFor("flag"))
    }
}
