package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.TextRange
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutSetDetailPaneSetNameTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `名前なしから鉛筆で編集し確定とDoneと空欄を保存する`() {
        val state = mutableStateOf(ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, setNumber = 2))
        val saved = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = {},
                    onDelete = {},
                    onNameChanged = {
                        saved.add(it)
                        state.value = state.value.copy(name = it)
                    },
                    showHotkey = false,
                )
            }
        }
        rule.onNodeWithTag("manual_readout_set_name").assertTextEquals("セット 2")
        rule.onAllNodesWithText("セット 2").assertCountEquals(2)
        rule.onNodeWithContentDescription("セット名を編集").performClick()
        rule.onNodeWithTag("manual_readout_set_name_input").performTextReplacement("レース用")
        assertEquals(emptyList(), saved)
        rule.onNodeWithContentDescription("確定").performClick()
        rule.onNodeWithTag("manual_readout_set_name").assertTextEquals("レース用")
        rule.onAllNodesWithText("レース用").assertCountEquals(2)
        rule.onNodeWithTag("manual_readout_set_name").performClick()
        rule.onNodeWithTag("manual_readout_set_name_input").performTextReplacement("あ".repeat(20))
        rule.onNodeWithTag("manual_readout_set_name_input").performImeAction()
        rule.onNodeWithTag("manual_readout_set_name").assertTextEquals("あ".repeat(20))
        rule.onNodeWithContentDescription("セット名を編集").performClick()
        rule.onNodeWithTag("manual_readout_set_name_input").performTextReplacement("   " + "い".repeat(20))
        rule.onNodeWithTag("manual_readout_set_name_input").performImeAction()
        rule.onNodeWithTag("manual_readout_set_name").assertTextEquals("い".repeat(20))
        rule.onNodeWithContentDescription("セット名を編集").performClick()
        rule.onNodeWithTag("manual_readout_set_name_input").performTextReplacement("")
        rule.onNodeWithTag("manual_readout_set_name_input").performImeAction()
        rule.onNodeWithTag("manual_readout_set_name").assertTextEquals("セット 2")
        assertEquals(listOf("レース用", "あ".repeat(20), "い".repeat(20), ""), saved)
    }

    @Test
    fun `セット名のカウンターと超過入力拒否は既存文字を保持する`() {
        val saved = mutableListOf<String>()
        val name = "あ".repeat(20)
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetNameRow(name = name, number = 1, onNameChanged = { saved.add(it) })
            }
        }
        rule.onNodeWithContentDescription("セット名を編集").performClick()
        val input = rule.onNodeWithTag("manual_readout_set_name_input")
        rule.onNodeWithText("20/20", useUnmergedTree = true).assertTextEquals("20/20")
        for (position in listOf(20, 0, 10)) {
            input.performTextInputSelection(TextRange(position))
            input.performTextInput("追加")
            input.assertTextContains(name)
        }
        input.performTextReplacement("い".repeat(21))
        input.assertTextContains(name)
        input.performTextInputSelection(TextRange(0, 2))
        input.performTextInput("うえ")
        input.assertTextContains("うえ" + "あ".repeat(18))
        input.performTextReplacement("短い名前")
        rule.onNodeWithText("4/20", useUnmergedTree = true).assertTextEquals("4/20")
        input.performTextReplacement("")
        rule.onNodeWithText("0/20", useUnmergedTree = true).assertTextEquals("0/20")
        assertEquals(emptyList(), saved)
        input.performImeAction()
        assertEquals(listOf(""), saved)
    }

    @Test
    fun `名前編集のフォーカス喪失で一度だけ確定する`() {
        val saved = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState =
                        ManualReadoutSetDetailUiState(
                            simulatorId = Simulator.LmuWindows.id,
                            setNumber = 1,
                            name = "予選用",
                        ),
                    onAddItem = {},
                    onDelete = {},
                    onNameChanged = { saved.add(it) },
                    showHotkey = true,
                )
            }
        }
        rule.onNodeWithContentDescription("セット名を編集").performClick()
        rule.onNodeWithTag("manual_readout_set_name_input").performTextReplacement("決勝用")
        rule.onNodeWithTag("manual_readout_hotkey_key").performClick()
        rule.onNodeWithTag("manual_readout_set_name_input").assertDoesNotExist()
        assertEquals(listOf("決勝用"), saved)
    }

    @Test
    fun `名前のある競合セットはシミュレーター名と名前で表示する`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState =
                        ManualReadoutSetDetailUiState(
                            simulatorId = Simulator.Gt7Ps5.id,
                            setNumber = 1,
                            hotkeyConflict = ManualReadoutHotkeyConflict.OtherSet(Simulator.Gt7Ps5.id, 2, "燃料用"),
                        ),
                    onAddItem = {},
                    onDelete = {},
                    showHotkey = true,
                )
            }
        }
        rule.onNodeWithText("Gran Turismo 7（PS5） の「燃料用」で使用中のため割り当てできません").assertExists()
    }
}
