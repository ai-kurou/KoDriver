@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class OtherListPaneSpeechRecognitionSlotTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `音声認識のスロットを一覧の先頭に表示する`() {
        rule.setContent {
            OtherListPane(
                uiState = OtherListUiState(items = listOf(OtherListItemType.Voice)),
                onItemClick = {},
                onOverlayVisibleChange = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
                speechRecognitionSection = { Text("音声認識スロット") },
            )
        }

        rule.onNodeWithText("音声認識スロット").assertIsDisplayed()
        rule.onNodeWithText("読み上げ音声").assertIsDisplayed()
    }

    @Test
    fun `スロットを渡さない場合は音声認識を表示しない`() {
        rule.setContent {
            OtherListPane(
                uiState = OtherListUiState(items = listOf(OtherListItemType.Voice)),
                onItemClick = {},
                onOverlayVisibleChange = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }

        rule.onNodeWithText("音声認識スロット").assertDoesNotExist()
        rule.onNodeWithText("読み上げ音声").assertIsDisplayed()
    }
}
