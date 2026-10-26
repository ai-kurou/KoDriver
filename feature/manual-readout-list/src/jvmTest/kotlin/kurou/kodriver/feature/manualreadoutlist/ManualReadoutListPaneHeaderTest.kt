package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutListPaneHeaderTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `両セグメントは選択状態を示し未選択側のタップだけ切替を通知する`() {
        val playLayout = mutableStateOf(false)
        var toggleCount = 0
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState = ManualReadoutListUiState(playButtonLayoutEnabled = playLayout.value),
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                    onTogglePlayButtonLayout = {
                        toggleCount++
                        playLayout.value = !playLayout.value
                    },
                )
            }
        }
        val standard = rule.onNodeWithContentDescription("標準表示")
        val playButtons = rule.onNodeWithContentDescription("再生ボタン表示")
        standard.assertIsSelected().assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
        playButtons.assertIsNotSelected().assert(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton),
        )
        standard.performClick()
        rule.runOnIdle { assertEquals(0, toggleCount) }
        playButtons.performClick()
        rule.runOnIdle { assertEquals(1, toggleCount) }
        standard.assertIsNotSelected()
        playButtons.assertIsSelected().performClick()
        rule.runOnIdle { assertEquals(1, toggleCount) }
        standard.performClick()
        rule.runOnIdle { assertEquals(2, toggleCount) }
        standard.assertIsSelected()
        playButtons.assertIsNotSelected()
    }

    @Test
    fun `セット数は両表示で更新され0件では表示しない`() {
        val state = mutableStateOf(ManualReadoutListUiState(sets = listOf(ManualReadoutSet("first"))))
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState = state.value,
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                )
            }
        }
        rule.onNodeWithText("1セット").assertIsDisplayed()
        rule.runOnIdle {
            state.value = state.value.copy(sets = listOf(ManualReadoutSet("first"), ManualReadoutSet("second")))
        }
        rule.onNodeWithText("2セット").assertIsDisplayed()
        rule.runOnIdle { state.value = state.value.copy(playButtonLayoutEnabled = true) }
        rule.onNodeWithText("2セット").assertIsDisplayed()
        rule.runOnIdle { state.value = state.value.copy(sets = emptyList()) }
        rule.onNodeWithText("2セット").assertDoesNotExist()
        rule.onNodeWithText("0セット").assertDoesNotExist()
        rule.runOnIdle { state.value = state.value.copy(playButtonLayoutEnabled = false) }
        rule.onNodeWithText("0セット").assertDoesNotExist()
    }
}
