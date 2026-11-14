package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Rule
import org.junit.Test

class ManualReadoutHotkeyChipAnimationTest {
    @get:Rule
    val rule = createComposeRule()

    private val chipText = "Ctrl+Shift+A"

    @Test
    fun `ホットキーチップは消える時に縮みながら消え終わるまで残る`() {
        val visible = mutableStateOf(true)
        showHotkeyChipRow(visible)
        rule.waitForIdle()
        rule.onNodeWithText(chipText, useUnmergedTree = true).assertExists()

        rule.mainClock.autoAdvance = false
        visible.value = false
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithText(chipText, useUnmergedTree = true).assertExists()
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithText(chipText, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `ホットキーチップは現れる時に展開しながら表示される`() {
        val visible = mutableStateOf(false)
        showHotkeyChipRow(visible)
        rule.waitForIdle()
        rule.onNodeWithText(chipText, useUnmergedTree = true).assertDoesNotExist()

        rule.mainClock.autoAdvance = false
        visible.value = true
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithText(chipText, useUnmergedTree = true).assertExists()
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithText(chipText, useUnmergedTree = true).assertIsDisplayed()
    }

    private fun showHotkeyChipRow(visible: MutableState<Boolean>) {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState =
                        ManualReadoutListUiState(
                            sets =
                                listOf(
                                    ManualReadoutSet(
                                        id = "assigned",
                                        name = "ホットキー",
                                        hotkeyModifiers = ManualReadoutHotkeyModifiers.CTRL_SHIFT,
                                        hotkeyVirtualKeyCode = 0x41,
                                    ),
                                ),
                        ),
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                    showHotkeyChip = visible.value,
                )
            }
        }
    }
}
