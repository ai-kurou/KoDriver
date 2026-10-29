package kurou.kodriver.feature.otherfeedbackdetail

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OtherFeedbackDetailUiStateTest {
    @Test
    fun `送信可否は必須項目とメール形式と送信状態とクールダウンで決まる`() {
        val valid =
            OtherFeedbackDetailUiState(
                message = "本文",
                name = "Kurou",
                email = "user@example.com",
            )
        assertTrue(valid.canSend, "必須項目とメールアドレスが有効なら送信できる")

        val invalidEmail =
            OtherFeedbackDetailUiState(
                message = "本文",
                name = "Kurou",
                email = "invalid-email",
            )
        assertFalse(invalidEmail.canSend, "メールアドレスの形式が不正なら送信できない")

        val blankEmail =
            OtherFeedbackDetailUiState(
                message = "本文",
                name = "Kurou",
                email = "",
            )
        assertFalse(blankEmail.canSend, "メールアドレスが空なら送信できない")

        val sending =
            OtherFeedbackDetailUiState(
                message = "送信します",
                name = "Kurou",
                email = "user@example.com",
                sendStatus = FeedbackSendStatus.Sending,
            )
        assertFalse(sending.canSend, "送信中なら送信できない")

        val coolingDown =
            OtherFeedbackDetailUiState(
                message = "送信します",
                name = "Kurou",
                email = "user@example.com",
                isCoolingDown = true,
            )
        assertFalse(coolingDown.canSend, "クールダウン中なら送信できない")
    }

    @Test
    fun `メール形式エラーは入力済みでエラー表示中のときだけ表示する`() {
        val blankEmailWithError =
            OtherFeedbackDetailUiState(
                email = "",
                showEmailError = true,
            )
        assertFalse(
            blankEmailWithError.showEmailFormatError,
            "メールアドレスが未入力なら形式エラーを表示しない",
        )

        val invalidEmailWithoutError =
            OtherFeedbackDetailUiState(
                email = "invalid-email",
                showEmailError = false,
            )
        assertFalse(
            invalidEmailWithoutError.showEmailFormatError,
            "エラー表示が無効なら形式エラーを表示しない",
        )

        val invalidEmailWithError =
            OtherFeedbackDetailUiState(
                email = "invalid-email",
                showEmailError = true,
            )
        assertTrue(
            invalidEmailWithError.showEmailFormatError,
            "エラー表示中に不正なメールアドレスが入力済みなら形式エラーを表示する",
        )
    }
}
