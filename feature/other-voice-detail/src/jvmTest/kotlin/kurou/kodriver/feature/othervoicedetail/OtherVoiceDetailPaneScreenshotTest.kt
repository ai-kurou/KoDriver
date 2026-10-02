package kurou.kodriver.feature.othervoicedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
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
    fun `声あり`() = screenshot(OtherVoiceDetailUiState(listOf(voice), voice.id, isLoading = false))

    @Test
    fun `取得中`() = screenshot(OtherVoiceDetailUiState(isLoading = true))

    @Test
    fun `空の一覧と案内`() = screenshot(OtherVoiceDetailUiState(isLoading = false))

    @Test
    fun `保存済み不在`() =
        screenshot(OtherVoiceDetailUiState(listOf(voice), "missing", isLoading = false, savedVoiceMissing = true))

    private fun screenshot(uiState: OtherVoiceDetailUiState) =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            OtherVoiceDetailPaneContent(uiState = uiState)
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }
}
