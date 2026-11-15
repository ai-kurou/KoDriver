package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemKeys
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test

class ManualReadoutItemsCardAnimationTest {
    init {
        registerAllManualReadoutSimulators()
    }

    @get:Rule
    val rule = createComposeRule()

    private val emptyHint = "読み上げたい項目を追加してください"
    private val previewTag = "manual_readout_preview_sentence"

    @Test
    fun `項目を全て外すと空ヒントは展開しながら現れ、プレビューは縮みながら消え終わるまで残る`() {
        val selected: MutableState<List<String>> = mutableStateOf(emptyList())
        showItemsCard(selected)
        rule.waitForIdle()
        rule.onNodeWithText(emptyHint).assertExists()
        rule.onNodeWithTag(previewTag).assertDoesNotExist()

        rule.mainClock.autoAdvance = false
        selected.value = listOf(lmuWindowsManualReadoutItemKeys().first())
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithTag(previewTag, useUnmergedTree = true).assertExists()
        rule.onNodeWithText(emptyHint).assertExists()
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithText(emptyHint).assertDoesNotExist()
        rule.onNodeWithTag(previewTag).assertExists()

        selected.value = emptyList()
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithTag(previewTag, useUnmergedTree = true).assertExists()
        rule.onNodeWithText(emptyHint).assertExists()
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithTag(previewTag, useUnmergedTree = true).assertDoesNotExist()
        rule.onNodeWithText(emptyHint).assertExists()
    }

    private fun showItemsCard(selected: MutableState<List<String>>) {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState =
                        ManualReadoutSetDetailUiState(
                            simulatorId = Simulator.LmuWindows.id,
                            setNumber = 1,
                            availableItemKeys = lmuWindowsManualReadoutItemKeys(),
                            selectedItemKeys = selected.value,
                        ),
                    onAddItem = {},
                    onDelete = {},
                )
            }
        }
    }
}
