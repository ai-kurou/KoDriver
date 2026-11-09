package kurou.kodriver.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class SpeechRecognitionChipScreenshotTest {
    @Test
    fun `OFF`() =
        composeScreenshotTest {
            captureChip(SpeechRecognitionUiState())
        }

    @Test
    fun `ON待機中`() =
        composeScreenshotTest {
            captureChip(SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.Listening))
        }

    @Test
    fun `ONコマンド待ち`() =
        composeScreenshotTest {
            captureChip(SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.CommandWaiting))
        }

    @Test
    fun `エラー`() =
        composeScreenshotTest {
            captureChip(SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.PermissionDenied))
        }

    @Test
    fun `ON待機中の音量リング小`() =
        composeScreenshotTest {
            captureChip(
                SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.Listening, level = 0.4f),
            )
        }

    @Test
    fun `ONコマンド待ちの音量リング大`() =
        composeScreenshotTest {
            captureChip(
                SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.CommandWaiting, level = 0.8f),
            )
        }

    @Test
    fun `詳細の聞き取り中`() =
        composeScreenshotTest {
            captureDetail(
                SpeechRecognitionUiState(
                    enabled = true,
                    status = SpeechRecognitionStatus.Listening,
                    level = 0.6f,
                    recognized = listOf(listOf("燃料", "然料"), listOf("ブレーキ")),
                    devices = listOf("内蔵マイク", "USBマイク"),
                    deviceName = "内蔵マイク",
                ),
            )
        }

    @Test
    fun `詳細の停止中`() =
        composeScreenshotTest {
            captureDetail(SpeechRecognitionUiState())
        }

    private fun DesktopComposeUiTest.captureDetail(uiState: SpeechRecognitionUiState) {
        setContent {
            KoDriverTheme {
                Surface(
                    modifier = Modifier.requiredSize(360.dp, 300.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    SpeechRecognitionDetailContent(
                        uiState = uiState,
                        onEnabledChange = {},
                        onDeviceMenuOpen = {},
                        onDeviceSelected = {},
                        modifier = Modifier.padding(KoDriverSpacing.medium),
                        levelGaugeHeight = 12.dp,
                    )
                }
            }
        }
        onRoot().captureRoboImage()
    }

    private fun DesktopComposeUiTest.captureChip(uiState: SpeechRecognitionUiState) {
        setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(96.dp, 96.dp).padding(26.dp)) {
                        SpeechRecognitionChip(uiState = uiState)
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
