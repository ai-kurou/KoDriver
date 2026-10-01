package kurou.kodriver.feature.othervoicedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class OtherVoiceDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    private val onBack: () -> Unit = mockk()

    @Test
    fun `戻るボタンをタップするとonBackが呼ばれる`() {
        every { onBack() } returns Unit
        rule.setContent {
            MaterialTheme {
                OtherVoiceDetailPaneContent(
                    uiState = OtherVoiceDetailUiState,
                    canNavigateBack = true,
                    onBack = onBack,
                )
            }
        }

        rule.onNode(hasText("読み上げ音声")).assertExists()
        rule.onNode(hasContentDescription("戻る")).performClick()

        verify(exactly = 1) { onBack() }
        confirmVerified(onBack)
    }

    @Test
    fun `戻れない場合は戻るボタンを表示しない`() {
        rule.setContent {
            MaterialTheme {
                OtherVoiceDetailPaneContent(
                    uiState = OtherVoiceDetailUiState,
                    canNavigateBack = false,
                )
            }
        }

        rule.onNode(hasText("読み上げ音声")).assertExists()
        rule.onNode(hasContentDescription("戻る")).assertDoesNotExist()
    }
}
