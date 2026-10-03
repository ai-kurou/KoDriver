package kurou.kodriver.feature.otherlist

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class OtherListPaneVoiceTest {
    @get:Rule
    val rule = createComposeRule()

    private val onItemClick: (OtherListItemType) -> Unit = mockk()

    @Test
    fun `読み上げ音声にシステム既定を表示しクリックを通知する`() {
        every { onItemClick(OtherListItemType.Voice) } returns Unit
        rule.setContent {
            OtherListPane(
                uiState = OtherListUiState(items = listOf(OtherListItemType.Voice)),
                onItemClick = onItemClick,
                onOverlayVisibleChange = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }

        rule
            .onNodeWithText("読み上げ音声")
            .assertIsDisplayed()
            .assertIsNotSelected()
            .performClick()
        rule.onNodeWithText("読み上げ音声").assertIsNotSelected()

        rule.onAllNodesWithText("システム既定").assertCountEquals(1)
        verify(exactly = 1) { onItemClick(OtherListItemType.Voice) }
        confirmVerified(onItemClick)
    }

    @Test
    fun `保存済み音声IDは音声項目だけの副テキストに表示する`() {
        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.Volume, OtherListItemType.Voice),
                        voiceId = "saved-voice",
                    ),
                onItemClick = {},
                onOverlayVisibleChange = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }

        rule.onAllNodesWithText("saved-voice").assertCountEquals(1)
        rule.onNode(hasText("読み上げ音声") and hasText("saved-voice")).assertExists()
        rule.onNode(hasText("音量") and hasText("saved-voice")).assertDoesNotExist()
        rule.onNodeWithText("システム既定").assertDoesNotExist()
    }

    @Test
    fun `音量項目だけにアプリと端末の音量を表示する`() {
        rule.setContent {
            OtherListPane(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.Volume, OtherListItemType.Voice),
                        soundVolume = 80,
                        deviceVolume = 60,
                    ),
                onItemClick = {},
                onOverlayVisibleChange = {},
                onKeepScreenOnChange = {},
                onDynamicColorEnabledChange = {},
                onHapticFeedbackEnabledChange = {},
                onStartupEnabledChange = {},
            )
        }

        val summary = "アプリの音量: 80%　端末のマスター音量: 60%"
        rule.onAllNodesWithText(summary).assertCountEquals(1)
        rule.onNode(hasText("音量") and hasText(summary)).assertIsDisplayed()
        rule.onNode(hasText("読み上げ音声") and hasText(summary)).assertDoesNotExist()
    }
}
