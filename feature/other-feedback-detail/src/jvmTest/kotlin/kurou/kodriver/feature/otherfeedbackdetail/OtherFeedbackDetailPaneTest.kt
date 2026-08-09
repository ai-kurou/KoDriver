@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherfeedbackdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import kurou.kodriver.core.model.FeedbackType
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.Simulator
import kurou.kodriver.core.model.TelemetryLog
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
    fun `必須項目エラーを表示する`() {
        rule.setContent {
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

        rule.onNodeWithText("内容を入力してください").assertExists()
        rule.onNodeWithText("名前を入力してください").assertExists()
        rule.onNodeWithText("メールアドレスを入力してください").assertExists()
    }

    @Test
    fun `メールアドレスの形式エラーを表示する`() {
        rule.setContent {
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

        rule.onNodeWithText("有効なメールアドレスを入力してください").assertExists()
    }

    @Test
    fun `送信中を表示する`() {
        rule.setContent {
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

        rule.onNodeWithText("送信中").assertExists()
    }

    @Test
    fun `送信成功を表示する`() {
        rule.setContent {
            MaterialTheme {
                OtherFeedbackDetailPaneContent(
                    uiState = OtherFeedbackDetailUiState(isSent = true),
                )
            }
        }

        rule.onNodeWithText("フィードバックを送信しました。").assertExists()
    }

    @Test
    fun `送信失敗を表示する`() {
        rule.setContent {
            MaterialTheme {
                OtherFeedbackDetailPaneContent(
                    uiState = OtherFeedbackDetailUiState(sendFailed = true),
                )
            }
        }

        rule.onNodeWithText("送信に失敗しました。時間をおいてもう一度お試しください。").assertExists()
    }

    @Test
    fun `添付されたログのチップを表示する`() {
        rule.setContent {
            MaterialTheme {
                OtherFeedbackDetailPaneContent(
                    uiState =
                        OtherFeedbackDetailUiState(
                            attachedTelemetryLog =
                                TelemetryLog(
                                    id = 42L,
                                    createdAt = 0L,
                                    simulator = Simulator.LmuWindows,
                                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                                    telemetryJson = "",
                                ),
                        ),
                )
            }
        }

        rule.onNodeWithText("テレメトリログ #42 を添付").assertExists()
    }

    @Test
    fun `ログが未添付の場合はチップを表示しない`() {
        rule.setContent {
            MaterialTheme {
                OtherFeedbackDetailPaneContent(uiState = OtherFeedbackDetailUiState())
            }
        }

        rule.onNode(hasContentDescription("添付を解除")).assertDoesNotExist()
    }

    @Test
    fun `添付を解除するボタンをタップするとonDetachTelemetryLogが呼ばれる`() {
        var detachCount = 0
        rule.setContent {
            MaterialTheme {
                OtherFeedbackDetailPaneContent(
                    uiState =
                        OtherFeedbackDetailUiState(
                            attachedTelemetryLog =
                                TelemetryLog(
                                    id = 42L,
                                    createdAt = 0L,
                                    simulator = Simulator.LmuWindows,
                                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                                    telemetryJson = "",
                                ),
                        ),
                    onDetachTelemetryLog = { detachCount++ },
                )
            }
        }

        rule.onNode(hasContentDescription("添付を解除")).performClick()

        assertEquals(1, detachCount)
    }

    @Test
    fun `SentryのリンクをタップするとonOpenSentryが呼ばれる`() {
        var openSentryCount = 0
        rule.setContent {
            MaterialTheme {
                OtherFeedbackDetailPaneContent(
                    uiState = OtherFeedbackDetailUiState(),
                    onOpenSentry = { openSentryCount++ },
                )
            }
        }

        val node = rule.onNodeWithText("Sentry", substring = true)
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        node
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(textLayoutResults)
        val text =
            textLayoutResults
                .first()
                .layoutInput.text.text
        val sentryStart = text.indexOf("Sentry")
        val sentryBoundingBox = textLayoutResults.first().getBoundingBox(sentryStart)
        node.performTouchInput { click(sentryBoundingBox.center) }

        assertEquals(1, openSentryCount)
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
