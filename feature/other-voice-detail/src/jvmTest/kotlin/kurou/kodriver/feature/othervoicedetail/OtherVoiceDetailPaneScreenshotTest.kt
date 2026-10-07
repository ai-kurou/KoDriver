package kurou.kodriver.feature.othervoicedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.TextToSpeechVoice
import org.junit.Test

class OtherVoiceDetailPaneScreenshotTest {
    private val voice = TextToSpeechVoice("voice-a", "音声A", "ja-JP")

    @Test
    fun `デフォルト`() = screenshot(OtherVoiceDetailUiState(voices = listOf(voice), isLoading = false))

    @Test
    fun `声あり`() =
        screenshot(
            OtherVoiceDetailUiState(
                listOf(
                    voice,
                    TextToSpeechVoice("voice-b", "音声B", "ja-jp-x-jac-local"),
                    TextToSpeechVoice("voice-c", "音声C", "ja-JP"),
                ),
                voice.id,
                isLoading = false,
            ),
        )

    @Test
    fun `既定音声を除外した一覧`() =
        screenshot(
            OtherVoiceDetailUiState(
                voices = listOf(voice.copy(isDefault = true), TextToSpeechVoice("voice-b", "音声B", "ja-JP")),
                selectedVoiceId = voice.id,
                isLoading = false,
            ),
        )

    @Test
    fun `既定音声のみの案内`() =
        screenshot(OtherVoiceDetailUiState(voices = listOf(voice.copy(isDefault = true)), isLoading = false))

    @Test
    fun `取得中`() = screenshot(OtherVoiceDetailUiState(isLoading = true))

    @Test
    fun `空の一覧と案内`() = screenshot(OtherVoiceDetailUiState(isLoading = false))

    @Test
    fun `保存済み不在`() =
        screenshot(OtherVoiceDetailUiState(listOf(voice), "missing", isLoading = false, savedVoiceMissing = true))

    @Test
    fun `狭いペインの音声カード`() =
        screenshot(
            OtherVoiceDetailUiState(listOf(voice, TextToSpeechVoice("voice-b", "音声B", "ja-JP")), voice.id, false),
            width = 360.dp,
        )

    @Test
    fun `ダークテーマの音声カード`() =
        screenshot(
            OtherVoiceDetailUiState(
                listOf(voice, TextToSpeechVoice("voice-b", "長い名前の日本語音声B", "ja-jp-x-jac-local")),
                voice.id,
                false,
            ),
            darkTheme = true,
        )

    @Test
    fun `試聴中の音声カード`() =
        screenshot(
            OtherVoiceDetailUiState(
                voices = listOf(voice),
                selectedVoiceId = voice.id,
                isLoading = false,
                previewingVoiceId = voice.id,
            ),
        )

    private fun screenshot(
        uiState: OtherVoiceDetailUiState,
        width: Dp = 1560.dp,
        darkTheme: Boolean = false,
    ) = composeScreenshotTest {
        setContent {
            KoDriverTheme(darkTheme = darkTheme) {
                Surface {
                    Box(modifier = Modifier.requiredSize(width, 1080.dp)) {
                        OtherVoiceDetailPaneContent(uiState = uiState)
                    }
                }
            }
        }
        if (uiState.isLoading) {
            onNodeWithText("読み込み中…").assertExists()
            onNode(hasContentDescription("システム既定を試聴")).assertDoesNotExist()
        } else {
            onNode(hasContentDescription("システム既定を試聴")).assertIsEnabled()
        }
        if (uiState.previewingVoiceId == voice.id) {
            onNode(hasContentDescription("音声Aの試聴を停止")).assertIsEnabled()
            mainClock.autoAdvance = false
            mainClock.advanceTimeBy(200)
        }
        onRoot().captureRoboImage()
    }
}
