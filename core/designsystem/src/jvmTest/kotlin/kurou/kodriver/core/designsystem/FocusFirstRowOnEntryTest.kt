package kurou.kodriver.core.designsystem

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FocusFirstRowOnEntryTest {
    @get:Rule
    val rule = createComposeRule()

    private val keys = listOf("a", "b", "c")

    @Test
    fun `キーボード操作では表示されたとき先頭の行にフォーカスが当たる`() {
        rule.setContent {
            CompositionLocalProvider(LocalInputModeManager provides fakeInputModeManager(InputMode.Keyboard)) {
                Rows(firstItemKey = "a", enabled = true)
            }
        }
        rule.waitForIdle()

        assertTrue(isFocused("a"))
        assertFalse(isFocused("b"))
    }

    @Test
    fun `タッチ操作ではフォーカスを要求しない`() {
        rule.setContent {
            CompositionLocalProvider(LocalInputModeManager provides fakeInputModeManager(InputMode.Touch)) {
                Rows(firstItemKey = "a", enabled = true)
            }
        }
        rule.waitForIdle()

        assertFalse(isFocused("a"))
    }

    @Test
    fun `無効で表示されたときは後から有効になってもフォーカスを置かない`() {
        var enabled by mutableStateOf(false)

        rule.setContent {
            CompositionLocalProvider(LocalInputModeManager provides fakeInputModeManager(InputMode.Keyboard)) {
                Rows(firstItemKey = "a", enabled = enabled)
            }
        }
        rule.waitForIdle()
        enabled = true
        rule.waitForIdle()

        assertFalse(isFocused("a"))
    }

    @Test
    fun `先頭の行が後から決まったときに一度だけフォーカスを置く`() {
        var firstItemKey by mutableStateOf<String?>(null)

        rule.setContent {
            CompositionLocalProvider(LocalInputModeManager provides fakeInputModeManager(InputMode.Keyboard)) {
                Rows(firstItemKey = firstItemKey, enabled = true)
            }
        }
        rule.waitForIdle()
        assertFalse(isFocused("a"))

        firstItemKey = "a"
        rule.waitForIdle()
        assertTrue(isFocused("a"))
    }

    @Test
    fun `一度フォーカスを置いた後に先頭の行が変わってもフォーカスを奪わない`() {
        var firstItemKey by mutableStateOf<String?>("a")

        rule.setContent {
            CompositionLocalProvider(LocalInputModeManager provides fakeInputModeManager(InputMode.Keyboard)) {
                Rows(firstItemKey = firstItemKey, enabled = true)
            }
        }
        rule.waitForIdle()

        firstItemKey = "b"
        rule.waitForIdle()

        assertFalse(isFocused("b"))
    }

    @Composable
    private fun Rows(
        firstItemKey: String?,
        enabled: Boolean,
    ) {
        val focusState = remember { ListRowFocusState() }
        FocusFirstRowOnEntry(firstItemKey = firstItemKey, enabled = enabled, focusState = focusState)
        Column {
            keys.forEach { key ->
                BasicText(
                    text = key,
                    modifier =
                        Modifier
                            .testTag(key)
                            .focusRequester(focusState.requesterFor(key))
                            .focusable(),
                )
            }
        }
    }

    private fun isFocused(tag: String): Boolean =
        rule.onNodeWithTag(tag).fetchSemanticsNode().config[SemanticsProperties.Focused]

    private fun fakeInputModeManager(mode: InputMode): InputModeManager =
        object : InputModeManager {
            override val inputMode: InputMode = mode

            override fun requestInputMode(inputMode: InputMode): Boolean = false
        }
}
