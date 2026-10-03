package kurou.kodriver.feature.otherlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.model.ReadoutStartSoundType
import org.junit.Rule
import org.junit.Test

class OtherListPaneReadoutStartSoundTest {
    @get:Rule
    val rule = createComposeRule()

    private val onItemClick: (OtherListItemType) -> Unit = mockk()

    @Test
    fun `開始音の既定種別Formula無線を表示しクリックを通知する`() {
        every { onItemClick(OtherListItemType.ReadoutStartSound) } returns Unit
        setPane(OtherListUiState(items = listOf(OtherListItemType.ReadoutStartSound, OtherListItemType.Voice)))

        rule
            .onNode(hasText("読み上げ開始音") and hasText("Formula無線"))
            .assertIsDisplayed()
            .performClick()
        rule.onNode(hasText("読み上げ音声") and hasText("Formula無線")).assertDoesNotExist()
        rule.onNodeWithText("電子ノイズ").assertDoesNotExist()
        verify(exactly = 1) { onItemClick(OtherListItemType.ReadoutStartSound) }
        confirmVerified(onItemClick)
    }

    @Test
    fun `開始音に選択中の電子ノイズを表示する`() {
        setPane(
            OtherListUiState(
                items = listOf(OtherListItemType.ReadoutStartSound, OtherListItemType.Voice),
                readoutStartSoundType = ReadoutStartSoundType.ELECTRONIC_NOISE,
            ),
        )

        rule.onNode(hasText("読み上げ開始音") and hasText("電子ノイズ")).assertIsDisplayed()
        rule.onNode(hasText("読み上げ音声") and hasText("電子ノイズ")).assertDoesNotExist()
        rule.onNodeWithText("Formula無線").assertDoesNotExist()
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
