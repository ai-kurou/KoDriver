package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemKeys
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test

class ManualReadoutItemsEmptySwitchTest {
    init {
        registerAllManualReadoutSimulators()
    }

    @get:Rule
    val rule = createComposeRule()

    private val emptyHint = "読み上げたい項目を追加してください"
    private val noItemsText = "このシミュレーターで選べる項目はまだありません"

    @Test
    fun `選べる項目が無いと文言だけを表示し、項目があるとカードに切り替わって文言は消える`() {
        val available: MutableState<List<String>> = mutableStateOf(emptyList())
        showDetail(available)
        rule.waitForIdle()
        rule.onNodeWithText(noItemsText).assertExists()
        rule.onNodeWithText(emptyHint).assertDoesNotExist()

        available.value = lmuWindowsManualReadoutItemKeys()
        rule.waitForIdle()
        rule.onNodeWithText(noItemsText).assertDoesNotExist()
        rule.onNodeWithText(emptyHint).assertExists()
    }

    @Test
    fun `選べる項目がある間はカードを表示し、項目が無くなると文言だけになる`() {
        val available: MutableState<List<String>> = mutableStateOf(lmuWindowsManualReadoutItemKeys())
        showDetail(available)
        rule.waitForIdle()
        rule.onNodeWithText(noItemsText).assertDoesNotExist()
        rule.onNodeWithText(emptyHint).assertExists()

        available.value = emptyList()
        rule.waitForIdle()
        rule.onNodeWithText(noItemsText).assertExists()
        rule.onNodeWithText(emptyHint).assertDoesNotExist()
    }

    private fun showDetail(available: MutableState<List<String>>) {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState =
                        ManualReadoutSetDetailUiState(
                            simulatorId = Simulator.LmuWindows.id,
                            setNumber = 1,
                            availableItemKeys = available.value,
                            selectedItemKeys = emptyList(),
                        ),
                    onAddItem = {},
                    onDelete = {},
                )
            }
        }
    }
}
