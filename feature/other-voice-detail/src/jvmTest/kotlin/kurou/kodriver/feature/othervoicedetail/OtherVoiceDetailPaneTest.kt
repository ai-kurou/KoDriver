package kurou.kodriver.feature.othervoicedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OtherVoiceDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    private val haptic: HapticFeedback = mockk()
    private val onVoiceSelected: (String) -> Unit = mockk()
    private val onPreviewClicked: (String, String) -> Unit = mockk()
    private val onRetryClicked: () -> Unit = mockk()
    private val voice = TextToSpeechVoice("voice-a", "音声A", "ja-JP")

    private val onBack: () -> Unit = mockk()

    @Test
    fun `既定音声を日本語の一覧と件数から除外し保存済みなら既定を選択表示する`() {
        rule.setContent {
            MaterialTheme {
                OtherVoiceDetailPaneContent(
                    OtherVoiceDetailUiState(
                        voices = listOf(voice.copy(isDefault = true), TextToSpeechVoice("other", "音声B", "ja-JP")),
                        selectedVoiceId = voice.id,
                        isLoading = false,
                    ),
                )
            }
        }
        rule.onNodeWithText("音声A").assertDoesNotExist()
        rule.onNodeWithText("音声B").assertIsNotSelected()
        rule.onNodeWithText("1 件").assertExists()
        rule.onNodeWithText("システム既定").assertIsSelected()
        rule.onNodeWithText("保存済みの音声が見つからないため、システム既定で読み上げます。").assertDoesNotExist()
    }

    @Test
    fun `既定音声のみなら追加音声がない案内を表示する`() {
        rule.setContent {
            MaterialTheme {
                OtherVoiceDetailPaneContent(
                    OtherVoiceDetailUiState(voices = listOf(voice.copy(isDefault = true)), isLoading = false),
                )
            }
        }
        rule.onNodeWithText("音声A").assertDoesNotExist()
        rule.onNodeWithText("0 件").assertExists()
        rule.onNodeWithText("システム既定").assertIsSelected()
        rule.onNodeWithText("システム既定以外の日本語の音声が見つかりません。").assertExists()
        rule.onNodeWithText("再読み込み").assertExists()
        rule.onNodeWithText("日本語の音声が見つかりません。端末の設定で日本語の音声を追加してください。").assertDoesNotExist()
    }

    @Test
    fun `広いペインは2列で3件目を次の行に配置する`() {
        rule.setContent {
            MaterialTheme {
                Box(Modifier.requiredSize(1560.dp, 1080.dp)) {
                    OtherVoiceDetailPaneContent(
                        OtherVoiceDetailUiState(
                            voices =
                                listOf(
                                    voice,
                                    TextToSpeechVoice("voice-b", "音声B", "ja-JP"),
                                    TextToSpeechVoice("voice-c", "音声C", "ja-JP"),
                                ),
                            isLoading = false,
                        ),
                    )
                }
            }
        }
        val first = rule.onNodeWithText("音声A").fetchSemanticsNode().boundsInRoot
        val second = rule.onNodeWithText("音声B").fetchSemanticsNode().boundsInRoot
        val third = rule.onNodeWithText("音声C").fetchSemanticsNode().boundsInRoot
        assertEquals(first.top, second.top)
        assertTrue(second.left > first.right)
        assertEquals(first.left, third.left)
        assertTrue(third.top > first.bottom)
        rule.onNodeWithText("3 件").assertExists()
    }

    @Test
    fun `狭いペインは音声カードを1列で配置する`() {
        rule.setContent {
            MaterialTheme {
                Box(Modifier.requiredSize(360.dp, 1080.dp)) {
                    OtherVoiceDetailPaneContent(
                        OtherVoiceDetailUiState(
                            voices = listOf(voice, TextToSpeechVoice("voice-b", "音声B", "ja-JP")),
                            isLoading = false,
                        ),
                    )
                }
            }
        }
        val first = rule.onNodeWithText("音声A").fetchSemanticsNode().boundsInRoot
        val second = rule.onNodeWithText("音声B").fetchSemanticsNode().boundsInRoot
        assertEquals(first.left, second.left)
        assertTrue(second.top > first.bottom)
    }

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

        rule.onNodeWithText("既定").assertExists()
        rule.onNodeWithText("日本語の音声").assertExists()
        rule.onNodeWithText("1 件").assertExists()
        rule.onNodeWithText("ja-JP", useUnmergedTree = true).assertExists()
        rule.onNodeWithText("端末の設定に従う", useUnmergedTree = true).assertExists()
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
        rule.onNode(hasContentDescription("システム既定を試聴")).assertDoesNotExist()
        rule.onNodeWithText("システム既定").assertDoesNotExist()
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
    fun `各カードの試聴は音声IDとサンプル文と振動を通知し選択を変更しない`() {
        every { onPreviewClicked(voice.id, "これは読み上げ音声の試聴です。") } returns Unit
        every { onPreviewClicked(VOICE_ID_UNSPECIFIED, "これは読み上げ音声の試聴です。") } returns Unit
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                MaterialTheme {
                    OtherVoiceDetailPaneContent(
                        uiState = OtherVoiceDetailUiState(listOf(voice), voice.id, isLoading = false),
                        onVoiceSelected = onVoiceSelected,
                        onPreviewClicked = onPreviewClicked,
                    )
                }
            }
        }

        rule.onNode(hasContentDescription("音声Aを試聴")).assertIsEnabled().performClick()
        rule.onNode(hasContentDescription("システム既定を試聴")).assertIsEnabled().performClick()
        rule.onNodeWithText("音声A").assertIsSelected()

        verify(exactly = 1) { onPreviewClicked(voice.id, "これは読み上げ音声の試聴です。") }
        verify(exactly = 1) { onPreviewClicked(VOICE_ID_UNSPECIFIED, "これは読み上げ音声の試聴です。") }
        verify(exactly = 0) { onVoiceSelected(voice.id) }
        verify(exactly = 0) { onVoiceSelected(VOICE_ID_UNSPECIFIED) }
        verify(exactly = 2) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(onPreviewClicked, onVoiceSelected, haptic)
    }

    @Test
    fun `試聴中のカードだけ停止の説明を表示し同じ音声IDを通知する`() {
        every { onPreviewClicked(voice.id, "これは読み上げ音声の試聴です。") } returns Unit
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                MaterialTheme {
                    OtherVoiceDetailPaneContent(
                        uiState =
                            OtherVoiceDetailUiState(
                                voices = listOf(voice),
                                isLoading = false,
                                previewingVoiceId = voice.id,
                            ),
                        onPreviewClicked = onPreviewClicked,
                    )
                }
            }
        }
        rule.onNode(hasContentDescription("音声Aの試聴を停止")).assertIsEnabled().performClick()
        rule.onNode(hasContentDescription("音声Aを試聴")).assertDoesNotExist()
        rule.onNode(hasContentDescription("システム既定を試聴")).assertIsEnabled()
        rule.onNodeWithText("音声A").assertIsNotSelected()
        verify(exactly = 1) { onPreviewClicked(voice.id, "これは読み上げ音声の試聴です。") }
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(onPreviewClicked, haptic)
    }

    @Test
    fun `システム既定の試聴中も停止の説明を表示する`() {
        rule.setContent {
            MaterialTheme {
                OtherVoiceDetailPaneContent(
                    OtherVoiceDetailUiState(isLoading = false, previewingVoiceId = VOICE_ID_UNSPECIFIED),
                )
            }
        }
        rule.onNode(hasContentDescription("システム既定の試聴を停止")).assertIsEnabled()
        rule.onNode(hasContentDescription("システム既定を試聴")).assertDoesNotExist()
        rule.onNodeWithText("システム既定").assertIsSelected()
    }
}
