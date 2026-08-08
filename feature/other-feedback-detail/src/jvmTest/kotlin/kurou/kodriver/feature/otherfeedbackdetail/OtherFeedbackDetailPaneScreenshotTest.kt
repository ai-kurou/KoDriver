package kurou.kodriver.feature.otherfeedbackdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.FeedbackType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import org.junit.Test

class OtherFeedbackDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            capture()
        }

    @Test
    fun `入力済み`() =
        composeScreenshotTest {
            capture(
                uiState =
                    OtherFeedbackDetailUiState(
                        type = FeedbackType.FeatureRequest,
                        message = "音声読み上げの条件をもう少し細かく設定できるようにしてほしいです。",
                        name = "Kurou",
                        email = "user@example.com",
                    ),
            )
        }

    @Test
    fun `ログ添付済み`() =
        composeScreenshotTest {
            capture(
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

    private fun DesktopComposeUiTest.capture(uiState: OtherFeedbackDetailUiState = OtherFeedbackDetailUiState()) {
        setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                        OtherFeedbackDetailPaneContent(uiState = uiState)
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
