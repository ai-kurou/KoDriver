@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherfeedbackdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.domain.model.FeedbackType
import org.junit.Test
import kotlin.test.assertEquals

class OtherFeedbackDetailPaneTest {
    @Test
    fun `本文を入力して送信できる`() =
        composeScreenshotTest {
            var message by mutableStateOf("")
            var name by mutableStateOf("")
            var email by mutableStateOf("")
            var sendCount = 0
            setContent {
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

            onAllNodes(hasSetTextAction())[0].performTextInput("本文")
            onAllNodes(hasSetTextAction())[1].performTextInput("Kurou")
            onAllNodes(hasSetTextAction())[2].performTextInput("user@example.com")
            waitForIdle()
            onNodeWithText("送信").assertIsEnabled().performClick()

            assertEquals("本文", message)
            assertEquals("Kurou", name)
            assertEquals("user@example.com", email)
            assertEquals(1, sendCount)
        }

    @Test
    fun `種類を選択できる`() =
        composeScreenshotTest {
            var selectedType: FeedbackType? = null
            setContent {
                MaterialTheme {
                    OtherFeedbackDetailPaneContent(
                        uiState = OtherFeedbackDetailUiState(),
                        onTypeSelected = { selectedType = it },
                    )
                }
            }

            onNodeWithText("改善要望").performClick()

            assertEquals(FeedbackType.FeatureRequest, selectedType)
        }

    @Test
    fun `診断情報を送信する説明を表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    OtherFeedbackDetailPaneContent(uiState = OtherFeedbackDetailUiState())
                }
            }

            onNodeWithText("原因調査のため、アプリの状態も送信されます。").assertExists()
        }

    @Test
    fun `必須項目エラーを表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    OtherFeedbackDetailPaneContent(
                        uiState =
                            OtherFeedbackDetailUiState(
                                showMessageError = true,
                                showNameError = true,
                                showEmailError = true,
                            ),
                    )
                }
            }

            onNodeWithText("内容を入力してください").assertExists()
            onNodeWithText("名前を入力してください").assertExists()
            onNodeWithText("メールアドレスを入力してください").assertExists()
        }

    @Test
    fun `メールアドレスの形式エラーを表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    OtherFeedbackDetailPaneContent(
                        uiState =
                            OtherFeedbackDetailUiState(
                                email = "invalid-email",
                                showEmailError = true,
                            ),
                    )
                }
            }

            onNodeWithText("有効なメールアドレスを入力してください").assertExists()
        }

    @Test
    fun `送信中を表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    OtherFeedbackDetailPaneContent(
                        uiState =
                            OtherFeedbackDetailUiState(
                                message = "本文",
                                name = "Kurou",
                                email = "user@example.com",
                                isSending = true,
                            ),
                    )
                }
            }

            onNodeWithText("送信中").assertExists()
        }

    @Test
    fun `送信成功を表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    OtherFeedbackDetailPaneContent(
                        uiState = OtherFeedbackDetailUiState(isSent = true),
                    )
                }
            }

            onNodeWithText("フィードバックを送信しました。").assertExists()
        }

    @Test
    fun `送信失敗を表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    OtherFeedbackDetailPaneContent(
                        uiState = OtherFeedbackDetailUiState(sendFailed = true),
                    )
                }
            }

            onNodeWithText("送信に失敗しました。時間をおいてもう一度お試しください。").assertExists()
        }

    @Test
    fun `戻るボタンをタップするとonBackが呼ばれる`() =
        composeScreenshotTest {
            var backCount = 0
            setContent {
                MaterialTheme {
                    OtherFeedbackDetailPaneContent(
                        uiState = OtherFeedbackDetailUiState(),
                        onBack = { backCount++ },
                    )
                }
            }

            onNode(hasContentDescription("戻る")).performClick()

            assertEquals(1, backCount)
        }
}
