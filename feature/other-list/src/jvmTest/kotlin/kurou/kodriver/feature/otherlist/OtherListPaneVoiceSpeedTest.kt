package kurou.kodriver.feature.otherlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class OtherListPaneVoiceSpeedTest {
    @get:Rule
    val rule = createComposeRule()

    private val onItemClick: (OtherListItemType) -> Unit = mockk()

    @Test
    fun `読み上げ設定に既定速度を表示しタップしても選択を通知しない`() {
        setPane(OtherListUiState(items = listOf(OtherListItemType.VoiceSpeed)))

        rule.onNodeWithText("読み上げ設定").assertIsDisplayed()
        rule.onNodeWithText("1.0倍").assertIsDisplayed()
        rule
            .onNodeWithText("読み上げ速度")
            .assertIsDisplayed()
            .assertIsNotSelected()
            .performClick()
        rule.onNodeWithText("読み上げ速度").assertIsNotSelected()

        verify(exactly = 0) { onItemClick(OtherListItemType.VoiceSpeed) }
        confirmVerified(onItemClick)
    }

    @Test
    fun `保存済み速度を小数1桁に丸めて表示する`() {
        setPane(OtherListUiState(items = listOf(OtherListItemType.VoiceSpeed), voiceSpeed = 1.26f))

        rule.onNodeWithText("1.3倍").assertIsDisplayed()
        confirmVerified(onItemClick)
    }

    @Test
    fun `整数の速度にも小数1桁を表示する`() {
        setPane(OtherListUiState(items = listOf(OtherListItemType.VoiceSpeed), voiceSpeed = 2f))

        rule.onNodeWithText("2.0倍").assertIsDisplayed()
        confirmVerified(onItemClick)
    }

    private fun setPane(uiState: OtherListUiState) {
        rule.setContent {
            OtherListPane(
                uiState = uiState,
                onItemClick = onItemClick,
                onOverlayVisibleChange = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }
    }
}
