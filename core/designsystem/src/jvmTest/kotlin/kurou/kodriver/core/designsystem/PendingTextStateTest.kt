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
    fun `重複した保存待ちは末尾側の一致までまとめて除去する`() {
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
        assertEquals("A", state.currentText)

        savedText = "B"
        composeRule.waitForIdle()
        assertEquals("B", state.currentText)

        savedText = "外部の更新"
        composeRule.waitForIdle()
        assertEquals("外部の更新", state.currentText)
    }
}
