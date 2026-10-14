package kurou.kodriver.feature.manualreadoutsetdetail

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutSetDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `戻るボタンは戻れる場合だけ表示しクリックを通知する`() {
        val canNavigateBack = mutableStateOf(false)
        var backed = 0
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = ManualReadoutSetDetailUiState(setNumber = 1),
                    onToggleItem = {},
                    onDelete = {},
                    canNavigateBack = canNavigateBack.value,
                    onBack = { backed++ },
                )
            }
        }
        rule.onNodeWithContentDescription("戻る").assertDoesNotExist()
        rule.runOnIdle { canNavigateBack.value = true }
        rule.onNodeWithContentDescription("戻る").assertExists().performClick()
        rule.runOnIdle { assertEquals(1, backed) }
    }

    @Test
    fun `選択と削除確認のキャンセルと確定を通知する`() {
        val state = mutableStateOf(ManualReadoutSetDetailUiState(setNumber = 2, availableItemKeys = listOf("energy")))
        var deleted = 0
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onToggleItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = { deleted++ },
                )
            }
        }
        rule.onNode(isToggleable()).assertIsOff().performClick()
        rule.onNode(isToggleable()).assertIsOn()
        rule.onNodeWithText("セットを削除").performClick()
        rule.onNodeWithText("キャンセル").performClick()
        rule.runOnIdle { assertEquals(0, deleted) }
        rule.onNodeWithText("セットを削除").performClick()
        rule.onNodeWithText("削除").performClick()
        rule.runOnIdle { assertEquals(1, deleted) }
    }

    @Test
    fun `未対応項目と見つからない状態を表示する`() {
        val state = mutableStateOf(ManualReadoutSetDetailUiState(setNumber = 1, isLoading = false))
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(state.value, {}, {})
            }
        }
        rule.onNodeWithText("このシミュレーターで選べる項目はまだありません").assertExists()
        rule.runOnIdle { state.value = ManualReadoutSetDetailUiState(isLoading = false) }
        rule.onNodeWithText("セットが見つかりません").assertExists()
        rule.onNodeWithText("セットを削除").assertDoesNotExist()
    }
}
