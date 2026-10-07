package kurou.kodriver.core.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class PendingTextStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `初期値は保存値で編集前は保存値に追従する`() {
        var savedText by mutableStateOf("初期値")
        lateinit var state: PendingTextState
        composeRule.setContent {
            state = rememberPendingText(savedText, maxLength = 10)
        }

        composeRule.waitForIdle()
        assertEquals("初期値", state.currentText)
        val initialState = state

        savedText = "更新値"
        composeRule.waitForIdle()
        assertEquals("更新値", state.currentText)
        assertSame(initialState, state)
    }

    @Test
    fun `編集中は古い保存値を無視し正規化後の保存値が一致すると追従を再開する`() {
        var savedText by mutableStateOf("初期値")
        lateinit var state: PendingTextState
        composeRule.setContent {
            state = rememberPendingText(savedText, maxLength = 5)
        }

        composeRule.waitForIdle()
        composeRule.runOnIdle { state.change("  abcdef  ") }
        composeRule.waitForIdle()
        assertEquals("  abcdef  ", state.currentText)

        savedText = "古い保存値"
        composeRule.waitForIdle()
        assertEquals("  abcdef  ", state.currentText)

        savedText = "abcdef"
        composeRule.waitForIdle()
        assertEquals("  abcdef  ", state.currentText)

        savedText = "abcde"
        composeRule.waitForIdle()
        assertEquals("abcde", state.currentText)

        savedText = "次の値"
        composeRule.waitForIdle()
        assertEquals("次の値", state.currentText)
    }

    @Test
    fun `リセット相当の変更も前後空白を除いた保存値を待つ`() {
        var savedText by mutableStateOf("保存済み")
        lateinit var state: PendingTextState
        composeRule.setContent {
            state = rememberPendingText(savedText, maxLength = 10)
        }

        composeRule.waitForIdle()
        composeRule.runOnIdle { state.change("編集中") }
        composeRule.waitForIdle()
        composeRule.runOnIdle { state.change("  デフォルト  ") }
        composeRule.waitForIdle()
        assertEquals("  デフォルト  ", state.currentText)

        savedText = "編集中"
        composeRule.waitForIdle()
        assertEquals("  デフォルト  ", state.currentText)

        savedText = "デフォルト"
        composeRule.waitForIdle()
        assertEquals("デフォルト", state.currentText)

        savedText = "外部の更新"
        composeRule.waitForIdle()
        assertEquals("外部の更新", state.currentText)
    }

    @Test
    fun `空白だけの入力は空の保存値に一致すると追従を再開する`() {
        var savedText by mutableStateOf("保存済み")
        lateinit var state: PendingTextState
        composeRule.setContent {
            state = rememberPendingText(savedText, maxLength = 0)
        }

        composeRule.waitForIdle()
        composeRule.runOnIdle { state.change("  ") }
        composeRule.waitForIdle()
        assertEquals("  ", state.currentText)

        savedText = ""
        composeRule.waitForIdle()
        assertEquals("", state.currentText)

        savedText = "更新値"
        composeRule.waitForIdle()
        assertEquals("更新値", state.currentText)
    }

    @Test
    fun `保存値が変わらなくても入力の正規化後の値が一致すれば保存値を表示し保存待ちを追加しない`() {
        var savedText by mutableStateOf("保存済み")
        lateinit var state: PendingTextState
        composeRule.setContent {
            state = rememberPendingText(savedText, maxLength = 10)
        }

        composeRule.waitForIdle()
        composeRule.runOnIdle { state.change("  保存済み  ") }
        composeRule.waitForIdle()
        assertEquals("保存済み", state.currentText)

        savedText = "更新値"
        composeRule.waitForIdle()
        assertEquals("更新値", state.currentText)
    }

    @Test
    fun `保存反映前に既定値へリセットしても途中の保存値に巻き戻らない`() {
        var savedText by mutableStateOf("D")
        lateinit var state: PendingTextState
        composeRule.setContent {
            state = rememberPendingText(savedText, maxLength = 10)
        }

        composeRule.waitForIdle()
        composeRule.runOnIdle {
            state.change("Q")
            state.change("D")
            state.updateSavedText("D")
        }
        composeRule.waitForIdle()
        assertEquals("D", state.currentText)

        savedText = "Q"
        composeRule.waitForIdle()
        assertEquals("D", state.currentText)

        savedText = "D"
        composeRule.waitForIdle()
        assertEquals("D", state.currentText)

        savedText = "外部の更新"
        composeRule.waitForIdle()
        assertEquals("外部の更新", state.currentText)
    }

    @Test
    fun `連続編集の保存値を順に観測しても最新の入力を保持する`() {
        var savedText by mutableStateOf("初期値")
        lateinit var state: PendingTextState
        composeRule.setContent {
            state = rememberPendingText(savedText, maxLength = 10)
        }

        composeRule.waitForIdle()
        composeRule.runOnIdle {
            state.change("A")
            state.change("B")
            state.change("C")
        }
        composeRule.waitForIdle()
        assertEquals("C", state.currentText)

        savedText = "A"
        composeRule.waitForIdle()
        assertEquals("C", state.currentText)

        savedText = "古い保存値"
        composeRule.waitForIdle()
        assertEquals("C", state.currentText)

        savedText = "B"
        composeRule.waitForIdle()
        assertEquals("C", state.currentText)

        savedText = "C"
        composeRule.waitForIdle()
        assertEquals("C", state.currentText)

        savedText = "外部の更新"
        composeRule.waitForIdle()
        assertEquals("外部の更新", state.currentText)
    }

    @Test
    fun `重複した保存待ちは先頭側の一致まで除去し最新の入力を保持する`() {
        var savedText by mutableStateOf("初期値")
        lateinit var state: PendingTextState
        composeRule.setContent {
            state = rememberPendingText(savedText, maxLength = 10)
        }

        composeRule.waitForIdle()
        composeRule.runOnIdle {
            state.change("A")
            state.change("B")
            state.change("  A  ")
        }
        composeRule.waitForIdle()

        savedText = "A"
        composeRule.waitForIdle()
        assertEquals("  A  ", state.currentText)

        savedText = "B"
        composeRule.waitForIdle()
        assertEquals("  A  ", state.currentText)

        savedText = "A"
        composeRule.waitForIdle()
        assertEquals("A", state.currentText)

        savedText = "外部の更新"
        composeRule.waitForIdle()
        assertEquals("外部の更新", state.currentText)
    }

    @Test
    fun `保存反映がconflateされても既定の期限後は外部更新に追従する`() {
        composeRule.mainClock.autoAdvance = false
        var savedText by mutableStateOf("D")
        lateinit var state: PendingTextState
        composeRule.setContent {
            state = rememberPendingText(savedText, maxLength = 10)
        }

        composeRule.runOnIdle {
            state.change("Q")
            state.change("D")
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(2000)
        composeRule.runOnIdle {
            assertEquals("D", savedText)
            assertEquals("D", state.currentText)
        }

        savedText = "期限前の外部更新"
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.runOnIdle { assertEquals("D", state.currentText) }

        composeRule.mainClock.advanceTimeBy(1100)
        composeRule.runOnIdle { assertEquals("D", state.currentText) }
        savedText = "外部の更新"
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.runOnIdle { assertEquals("外部の更新", state.currentText) }
    }

    @Test
    fun `保存値の一致で解除されたタイマーは次の保存待ちに影響しない`() {
        composeRule.mainClock.autoAdvance = false
        var savedText by mutableStateOf("初期値")
        lateinit var state: PendingTextState
        composeRule.setContent {
            state = rememberPendingText(savedText, maxLength = 10, pendingTimeoutMillis = 1000L)
        }

        composeRule.runOnIdle { state.change("  A  ") }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(800)
        savedText = "A"
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.runOnIdle {
            assertEquals("A", state.currentText)
            state.change("  B  ")
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(300)
        savedText = "外部の更新"
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.runOnIdle { assertEquals("  B  ", state.currentText) }

        savedText = "B"
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.runOnIdle { assertEquals("B", state.currentText) }
        composeRule.mainClock.advanceTimeBy(1100)
        savedText = "次の外部更新"
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.runOnIdle { assertEquals("次の外部更新", state.currentText) }
    }

    @Test
    fun `保存待ちが追加されるとタイムアウトの期限を再設定する`() {
        composeRule.mainClock.autoAdvance = false
        var savedText by mutableStateOf("初期値")
        lateinit var state: PendingTextState
        composeRule.setContent {
            state = rememberPendingText(savedText, maxLength = 10, pendingTimeoutMillis = 1000L)
        }

        composeRule.runOnIdle { state.change("A") }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(900)
        composeRule.runOnIdle { state.change("  B  ") }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(200)
        savedText = "期限前の外部更新"
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.runOnIdle { assertEquals("  B  ", state.currentText) }

        composeRule.mainClock.advanceTimeBy(900)
        composeRule.runOnIdle { assertEquals("  B  ", state.currentText) }
        savedText = "外部の更新"
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.runOnIdle { assertEquals("外部の更新", state.currentText) }
    }
}
