package kurou.kodriver.feature.othervoicedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import org.junit.Rule
import org.junit.Test

class OtherVoiceDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    private val haptic: HapticFeedback = mockk()
    private val onVoiceSelected: (String) -> Unit = mockk()
    private val onPreviewClicked: (String) -> Unit = mockk()
    private val onRetryClicked: () -> Unit = mockk()
    private val voice = TextToSpeechVoice("voice-a", "音声A", "ja-JP")

    private val onBack: () -> Unit = mockk()

    @Test
    fun `戻るボタンをタップするとonBackが呼ばれる`() {
        every { onBack() } returns Unit
        rule.setContent {
            MaterialTheme {
                OtherVoiceDetailPaneContent(
                    uiState = OtherVoiceDetailUiState(),
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
                    uiState = OtherVoiceDetailUiState(),
                    canNavigateBack = false,
                )
            }
        }

        rule.onNode(hasText("読み上げ音声")).assertExists()
        rule.onNode(hasContentDescription("戻る")).assertDoesNotExist()
    }

    @Test
    fun `音声一覧を表示し選択を通知する`() {
        every { onVoiceSelected(voice.id) } returns Unit
        every { onVoiceSelected(VOICE_ID_UNSPECIFIED) } returns Unit
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                MaterialTheme {
                    OtherVoiceDetailPaneContent(
                        uiState = OtherVoiceDetailUiState(listOf(voice), voice.id, isLoading = false),
                        onVoiceSelected = onVoiceSelected,
                    )
                }
            }
        }

        rule.onNodeWithText("音声A").assertIsSelected().performClick()
        rule.onNodeWithText("システム既定").assertIsNotSelected().performClick()
        rule.onNodeWithText("再読み込み").assertDoesNotExist()
        verify(exactly = 1) { onVoiceSelected(voice.id) }
        verify(exactly = 1) { onVoiceSelected(VOICE_ID_UNSPECIFIED) }
        verify(exactly = 2) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(onVoiceSelected, haptic)
    }

    @Test
    fun `取得中は読み込み中の文言を表示し空の案内と再読み込みを表示しない`() {
        rule.setContent { MaterialTheme { OtherVoiceDetailPaneContent(OtherVoiceDetailUiState()) } }

        rule.onNodeWithText("読み込み中…").assertExists()
        rule.onNodeWithText("試聴").assertIsNotEnabled()
        rule.onNodeWithText("システム既定").assertIsSelected()
        rule.onNodeWithText("再読み込み").assertDoesNotExist()
        rule
            .onNodeWithText("日本語の音声が見つかりません。端末の設定で日本語の音声を追加してください。")
            .assertDoesNotExist()
    }

    @Test
    fun `空の一覧は案内と再読み込みを表示しクリックを通知する`() {
        every { onRetryClicked() } returns Unit
        rule.setContent {
            MaterialTheme {
                OtherVoiceDetailPaneContent(
                    uiState = OtherVoiceDetailUiState(isLoading = false),
                    onRetryClicked = onRetryClicked,
                )
            }
        }

        rule.onNodeWithText("読み込み中…").assertDoesNotExist()
        rule
            .onNodeWithText("日本語の音声が見つかりません。端末の設定で日本語の音声を追加してください。")
            .assertExists()
        rule.onNodeWithText("再読み込み").performClick()
        verify(exactly = 1) { onRetryClicked() }
        confirmVerified(onRetryClicked)
    }

    @Test
    fun `保存済み音声が不在の場合は注意文とシステム既定の選択を表示する`() {
        rule.setContent {
            MaterialTheme {
                OtherVoiceDetailPaneContent(
                    OtherVoiceDetailUiState(listOf(voice), "missing", isLoading = false, savedVoiceMissing = true),
                )
            }
        }

        rule
            .onNodeWithText("保存済みの音声が見つからないため、システム既定で読み上げます。")
            .assertExists()
        rule.onNodeWithText("システム既定").assertIsSelected()
        rule.onNodeWithText("音声A").assertIsNotSelected()
    }

    @Test
    fun `試聴ボタンはサンプル文と振動を通知する`() {
        every { onPreviewClicked("これは読み上げ音声の試聴です。") } returns Unit
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                MaterialTheme {
                    OtherVoiceDetailPaneContent(
                        uiState = OtherVoiceDetailUiState(listOf(voice), voice.id, isLoading = false),
                        onPreviewClicked = onPreviewClicked,
                    )
                }
            }
        }

        rule.onNodeWithText("試聴").assertIsEnabled().performClick()

        verify(exactly = 1) { onPreviewClicked("これは読み上げ音声の試聴です。") }
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(onPreviewClicked, haptic)
    }
}
