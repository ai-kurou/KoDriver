package kurou.kodriver.core.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class PendingThresholdStateTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `初期値と未操作時の保存値を反映し同じ値では保存待ちを追加しない`() {
        val state = PendingThresholdState(3)
        assertEquals(3, state.currentValue)
        state.finishChange(3)
        assertEquals(emptyList(), state.pendingValues)
        state.updateSavedValue(3)
        state.updateSavedValue(2)
        assertEquals(2, state.currentValue)
    }

    @Test
    fun `途中の保存通知と不一致の通知で最新操作を巻き戻さず最後の一致で追従する`() {
        val state = PendingThresholdState(3)
        state.change(5)
        state.finishChange(5)
        state.change(4)
        state.finishChange(4)
        state.updateSavedValue(5)
        assertEquals(4, state.currentValue)
        assertEquals(listOf(4), state.pendingValues)
        state.updateSavedValue(2)
        assertEquals(4, state.currentValue)
        state.updateSavedValue(4)
        assertEquals(emptyList(), state.pendingValues)
        state.updateSavedValue(1)
        assertEquals(1, state.currentValue)
    }

    @Test
    fun `次のドラッグ中は先の保存が完了しても操作中の値を保持する`() {
        val state = PendingThresholdState(3)
        state.finishChange(5)
        state.change(4)
        state.updateSavedValue(5)
        assertEquals(emptyList(), state.pendingValues)
        assertEquals(4, state.currentValue)
        state.expirePending()
        state.updateSavedValue(2)
        assertEquals(4, state.currentValue)
        state.finishChange(4)
        state.updateSavedValue(4)
        state.updateSavedValue(1)
        assertEquals(1, state.currentValue)
    }

    @Test
    fun `保存通知前のリセットと重複値も先頭の一致だけを解除する`() {
        val state = PendingThresholdState(3)
        state.finishChange(5)
        state.finishChange(3)
        state.finishChange(5)
        state.updateSavedValue(3)
        assertEquals(listOf(5, 3, 5), state.pendingValues)
        state.updateSavedValue(5)
        assertEquals(listOf(3, 5), state.pendingValues)
        assertEquals(5, state.currentValue)
        state.updateSavedValue(3)
        assertEquals(5, state.currentValue)
        state.updateSavedValue(5)
        assertEquals(emptyList(), state.pendingValues)
    }

    @Test
    fun `保存値に一致するドラッグを完了すると未操作時の追従を再開する`() {
        val state = PendingThresholdState(3)
        state.change(4)
        state.change(3)
        state.finishChange(3)
        assertEquals(emptyList(), state.pendingValues)
        state.updateSavedValue(2)
        assertEquals(2, state.currentValue)
    }

    @Test
    fun `保存通知の省略時は期限後も表示値を保持し次の外部更新に追従する`() {
        rule.mainClock.autoAdvance = false
        var savedValue by mutableIntStateOf(3)
        lateinit var state: PendingThresholdState
        rule.setContent { state = rememberPendingThreshold(savedValue) }
        val initialState = state
        rule.runOnIdle {
            state.finishChange(5)
            state.finishChange(3)
        }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(2000)
        savedValue = 2
        rule.mainClock.advanceTimeByFrame()
        rule.runOnIdle { assertEquals(3, state.currentValue) }
        rule.mainClock.advanceTimeBy(1100)
        rule.runOnIdle {
            assertEquals(3, state.currentValue)
            assertEquals(emptyList(), state.pendingValues)
        }
        savedValue = 4
        rule.mainClock.advanceTimeByFrame()
        rule.runOnIdle {
            assertEquals(4, state.currentValue)
            assertSame(initialState, state)
        }
    }

    @Test
    fun `保存待ちが増えると期限を更新し保存完了後のタイマーは次の操作に影響しない`() {
        rule.mainClock.autoAdvance = false
        var savedValue by mutableIntStateOf(3)
        lateinit var state: PendingThresholdState
        rule.setContent { state = rememberPendingThreshold(savedValue, pendingTimeoutMillis = 1000L) }
        rule.runOnIdle { state.finishChange(5) }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(800)
        rule.runOnIdle { state.finishChange(4) }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(300)
        savedValue = 2
        rule.mainClock.advanceTimeByFrame()
        rule.runOnIdle { assertEquals(4, state.currentValue) }
        savedValue = 4
        rule.mainClock.advanceTimeByFrame()
        rule.runOnIdle {
            assertEquals(emptyList(), state.pendingValues)
            state.finishChange(5)
        }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(800)
        savedValue = 3
        rule.mainClock.advanceTimeByFrame()
        rule.runOnIdle { assertEquals(5, state.currentValue) }
        rule.mainClock.advanceTimeBy(300)
        savedValue = 1
        rule.mainClock.advanceTimeByFrame()
        rule.runOnIdle { assertEquals(1, state.currentValue) }
    }
}
