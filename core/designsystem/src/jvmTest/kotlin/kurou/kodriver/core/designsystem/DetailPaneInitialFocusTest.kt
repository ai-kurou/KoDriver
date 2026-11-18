package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DetailPaneInitialFocusTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `キーボード操作のときだけ初期フォーカスを要求する`() {
        assertTrue(shouldRequestInitialFocus(InputMode.Keyboard))
        assertFalse(shouldRequestInitialFocus(InputMode.Touch))
    }

    @Test
    fun `キーボード操作では本文先頭のボタンにフォーカスが当たる`() {
        rule.setContent {
            KoDriverTheme {
                CompositionLocalProvider(LocalInputModeManager provides fakeInputModeManager(InputMode.Keyboard)) {
                    DetailPaneScaffold(title = "ライセンス", canNavigateBack = true, onBack = {}) {
                        Column {
                            Button(onClick = {}, modifier = Modifier.testTag(TAG_FIRST_BUTTON)) { Text("先頭") }
                            Button(onClick = {}) { Text("次") }
                        }
                    }
                }
            }
        }
        rule.waitForIdle()

        assertTrue(rule.onNodeWithTag(TAG_FIRST_BUTTON).fetchSemanticsNode().config[SemanticsProperties.Focused])
    }

    @Test
    fun `タッチ操作では初期フォーカスを要求しない`() {
        rule.setContent {
            KoDriverTheme {
                CompositionLocalProvider(LocalInputModeManager provides fakeInputModeManager(InputMode.Touch)) {
                    DetailPaneScaffold(title = "ライセンス", canNavigateBack = true, onBack = {}) {
                        Button(onClick = {}, modifier = Modifier.testTag(TAG_FIRST_BUTTON)) { Text("先頭") }
                    }
                }
            }
        }
        rule.waitForIdle()

        assertFalse(rule.onNodeWithTag(TAG_FIRST_BUTTON).fetchSemanticsNode().config[SemanticsProperties.Focused])
    }

    private fun fakeInputModeManager(mode: InputMode): InputModeManager =
        object : InputModeManager {
            override val inputMode: InputMode = mode

            override fun requestInputMode(inputMode: InputMode): Boolean = false
        }

    private companion object {
        const val TAG_FIRST_BUTTON = "detail-pane-first-button"
    }
}
