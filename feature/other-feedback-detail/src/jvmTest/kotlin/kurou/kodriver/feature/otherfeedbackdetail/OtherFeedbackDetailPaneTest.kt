@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherfeedbackdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import kurou.kodriver.domain.model.FeedbackType
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherFeedbackDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `本文を入力して送信できる`() {
        var message by mutableStateOf("")
        var name by mutableStateOf("")
        var email by mutableStateOf("")
        var sendCount = 0
        rule.setContent {
            MaterialTheme {
                OtherFeedbackDetailPaneContent(
                    uiState =
                        OtherFeedbackDetailUiState(
                            message = message,
                            name = name,
                            email = email,
                        ),
                    onMessageChanged = { message = it },
                    onNameChanged = { name = it },
                    onEmailChanged = { email = it },
                    onSend = { sendCount++ },
                )
            }
        }

        rule.onAllNodes(hasSetTextAction())[0].performTextInput("本文")
        rule.onAllNodes(hasSetTextAction())[1].performTextInput("Kurou")
        rule.onAllNodes(hasSetTextAction())[2].performTextInput("user@example.com")
        rule.waitForIdle()
        rule.onNodeWithText("送信").assertIsEnabled().performClick()

        assertEquals("本文", message)
        assertEquals("Kurou", name)
        assertEquals("user@example.com", email)
        assertEquals(1, sendCount)
    }

    @Test
    fun `種類を選択できる`() {
        var selectedType: FeedbackType? = null
        rule.setContent {
            MaterialTheme {
                OtherFeedbackDetailPaneContent(
                    uiState = OtherFeedbackDetailUiState(),
                    onTypeSelected = { selectedType = it },
                )
            }
        }

        rule.onNodeWithText("改善要望").performClick()

        assertEquals(FeedbackType.FeatureRequest, selectedType)
    }

    @Test
    fun `診断情報を送信する説明を表示する`() {
        rule.setContent {
            MaterialTheme {
                OtherFeedbackDetailPaneContent(uiState = OtherFeedbackDetailUiState())
            }
        }

        rule.onNodeWithText("原因調査のため、アプリの状態も送信されます。").assertExists()
    }

    @Test
    fun `戻るボタンをタップするとonBackが呼ばれる`() {
        var backCount = 0
        rule.setContent {
            MaterialTheme {
                OtherFeedbackDetailPaneContent(
                    uiState = OtherFeedbackDetailUiState(),
                    onBack = { backCount++ },
                )
            }
        }

        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }
}
