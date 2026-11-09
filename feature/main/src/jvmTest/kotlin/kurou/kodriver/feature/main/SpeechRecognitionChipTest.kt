package kurou.kodriver.feature.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SpeechRecognitionChipTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `停止中はOFFに分類する`() {
        assertEquals(SpeechRecognitionChipState.Off, SpeechRecognitionUiState().toChipState())
    }

    @Test
    fun `有効で準備中と待機中はONに分類する`() {
        listOf(SpeechRecognitionStatus.Starting, SpeechRecognitionStatus.Listening).forEach { status ->
            assertEquals(
                SpeechRecognitionChipState.On,
                SpeechRecognitionUiState(enabled = true, status = status).toChipState(),
            )
        }
    }

    @Test
    fun `コマンド待ちはコマンド待ちに分類する`() {
        assertEquals(
            SpeechRecognitionChipState.CommandWaiting,
            SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.CommandWaiting).toChipState(),
        )
    }

    @Test
    fun `聞き取れない状態はすべてエラーに分類する`() {
        listOf(
            SpeechRecognitionStatus.PermissionDenied,
            SpeechRecognitionStatus.Unavailable,
            SpeechRecognitionStatus.MicrophoneUnavailable,
            SpeechRecognitionStatus.NoSignal,
            SpeechRecognitionStatus.RecognitionFailed,
        ).forEach { status ->
            assertEquals(
                SpeechRecognitionChipState.Error,
                SpeechRecognitionUiState(enabled = true, status = status).toChipState(),
            )
        }
    }

    @Test
    fun `停止中は停止中の説明を付けて表示する`() {
        rule.setContent { KoDriverTheme { SpeechRecognitionChip(uiState = SpeechRecognitionUiState()) } }

        rule.onNodeWithContentDescription("停止中").assertIsDisplayed()
    }

    @Test
    fun `聞き取り中は呼びかけ待ちの説明を付けて表示する`() {
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionChip(
                    uiState = SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.Listening),
                )
            }
        }

        rule.onNodeWithContentDescription("「コドライバー」の呼びかけを待っています(マイクを使用中)").assertIsDisplayed()
    }

    @Test
    fun `コマンド待ちはコマンド聞き取り中の説明を付けて表示する`() {
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionChip(
                    uiState = SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.CommandWaiting),
                )
            }
        }

        rule.onNodeWithContentDescription("コマンドを聞き取り中(マイクを使用中)").assertIsDisplayed()
    }

    @Test
    fun `エラー時は原因の説明を付けて表示する`() {
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionChip(
                    uiState = SpeechRecognitionUiState(status = SpeechRecognitionStatus.PermissionDenied),
                )
            }
        }

        rule.onNodeWithContentDescription("マイクの権限が許可されていません").assertIsDisplayed()
    }
}
