package kurou.kodriver.feature.otherfeedbackdetail

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OtherFeedbackDetailUiStateTest {
    @Test
    fun `必須項目が全て有効なメールアドレスとともに入力されていれば送信できる`() {
        val uiState =
            OtherFeedbackDetailUiState(
                message = "本文",
                name = "Kurou",
                email = "user@example.com",
            )

        assertTrue(uiState.canSend)
    }

    @Test
    fun `メールアドレスの形式が不正なら送信できない`() {
        val uiState =
            OtherFeedbackDetailUiState(
                message = "本文",
                name = "Kurou",
                email = "invalid-email",
            )

        assertFalse(uiState.canSend)
    }

    @Test
    fun `メールアドレスが空なら送信できない`() {
        val uiState =
            OtherFeedbackDetailUiState(
                message = "本文",
                name = "Kurou",
                email = "",
            )

        assertFalse(uiState.canSend)
    }

    @Test
    fun `エラー表示中かつメールアドレスが未入力なら形式エラーは表示しない`() {
        val uiState =
            OtherFeedbackDetailUiState(
                email = "",
                showEmailError = true,
            )

        assertFalse(uiState.showEmailFormatError)
    }

    @Test
    fun `エラー未表示なら形式エラーは表示しない`() {
        val uiState =
            OtherFeedbackDetailUiState(
                email = "invalid-email",
                showEmailError = false,
            )

        assertFalse(uiState.showEmailFormatError)
    }

    @Test
    fun `エラー表示中かつメールアドレスが入力済みなら形式エラーを表示する`() {
        val uiState =
            OtherFeedbackDetailUiState(
                email = "invalid-email",
                showEmailError = true,
            )

        assertTrue(uiState.showEmailFormatError)
    }

    @Test
    fun `送信中なら送信できない`() {
        val uiState =
            OtherFeedbackDetailUiState(
                message = "送信します",
                name = "Kurou",
                email = "user@example.com",
                sendStatus = FeedbackSendStatus.Sending,
            )

        assertFalse(uiState.canSend)
    }
}
