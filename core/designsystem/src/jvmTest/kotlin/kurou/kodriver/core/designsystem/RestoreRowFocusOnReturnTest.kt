package kurou.kodriver.core.designsystem

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RestoreRowFocusOnReturnTest {
    @get:Rule
    val rule = createComposeRule()

    private val keys = listOf("a", "b", "c")

    @Test
    fun `キーボード操作では復元キーの行にフォーカスを戻して通知する`() {
        var restoredCount = 0

        rule.setContent {
            CompositionLocalProvider(LocalInputModeManager provides fakeInputModeManager(InputMode.Keyboard)) {
                RowsWithRestore(restoreKey = "b", onRestored = { restoredCount++ })
            }
        }
        rule.waitForIdle()

        assertTrue(isFocused("b"))
        assertFalse(isFocused("a"))
        assertEquals(1, restoredCount)
    }

    @Test
    fun `タッチ操作ではフォーカスを要求せず通知だけする`() {
        var restoredCount = 0

        rule.setContent {
            CompositionLocalProvider(LocalInputModeManager provides fakeInputModeManager(InputMode.Touch)) {
                RowsWithRestore(restoreKey = "b", onRestored = { restoredCount++ })
            }
        }
        rule.waitForIdle()

        assertFalse(isFocused("b"))
        assertEquals(1, restoredCount)
    }

    @Test
    fun `復元キーが無いときは何もしない`() {
        var restoredCount = 0

        rule.setContent {
            CompositionLocalProvider(LocalInputModeManager provides fakeInputModeManager(InputMode.Keyboard)) {
                RowsWithRestore(restoreKey = null, onRestored = { restoredCount++ })
            }
        }
        rule.waitForIdle()

        keys.forEach { assertFalse(isFocused(it)) }
        assertEquals(0, restoredCount)
    }

    @Test
    fun `復元キーが後から設定されたときにフォーカスを戻す`() {
        var restoreKey by mutableStateOf<String?>(null)

        rule.setContent {
            CompositionLocalProvider(LocalInputModeManager provides fakeInputModeManager(InputMode.Keyboard)) {
                RowsWithRestore(restoreKey = restoreKey, onRestored = { restoreKey = null })
            }
        }
        rule.waitForIdle()
        assertFalse(isFocused("c"))

        restoreKey = "c"
        rule.waitForIdle()

        assertTrue(isFocused("c"))
    }

    @Test
    fun `画面外の行はスクロールしてからフォーカスを戻す`() {
        val manyKeys = List(ROW_COUNT) { "row$it" }

        rule.setContent {
            CompositionLocalProvider(LocalInputModeManager provides fakeInputModeManager(InputMode.Keyboard)) {
                val focusState = remember { ListRowFocusState() }
                val listState = rememberLazyListState()
                RestoreRowFocusOnReturn(
                    restoreItemKey = "row40",
                    focusState = focusState,
                    onRestored = {},
                    listState = listState,
                    lazyIndexOf = { key -> manyKeys.indexOf(key).takeIf { it >= 0 } },
                )
                LazyColumn(modifier = Modifier.height(100.dp), state = listState) {
                    items(manyKeys.size, key = { manyKeys[it] }) { index ->
                        BasicText(
                            text = manyKeys[index],
                            modifier =
                                Modifier
                                    .height(40.dp)
                                    .testTag(manyKeys[index])
                                    .focusRequester(focusState.requesterFor(manyKeys[index]))
                                    .focusable(),
                        )
                    }
                }
            }
        }
        rule.waitForIdle()

        assertTrue(isFocused("row40"))
    }

    @Test
    fun `一覧に無いキーのスクロール位置は求めずに通常のフォーカス要求にする`() {
        rule.setContent {
            CompositionLocalProvider(LocalInputModeManager provides fakeInputModeManager(InputMode.Keyboard)) {
                val focusState = remember { ListRowFocusState() }
                val listState = rememberLazyListState()
                RestoreRowFocusOnReturn(
                    restoreItemKey = "b",
                    focusState = focusState,
                    onRestored = {},
                    listState = listState,
                    lazyIndexOf = { null },
                )
                Rows(focusState)
            }
        }
        rule.waitForIdle()

        assertTrue(isFocused("b"))
    }

    @Composable
    private fun RowsWithRestore(
        restoreKey: String?,
        onRestored: () -> Unit,
    ) {
        val focusState = remember { ListRowFocusState() }
        RestoreRowFocusOnReturn(restoreItemKey = restoreKey, focusState = focusState, onRestored = onRestored)
        Rows(focusState)
    }

    @Composable
    private fun Rows(focusState: ListRowFocusState) {
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

    private companion object {
        const val ROW_COUNT = 60
    }
}
