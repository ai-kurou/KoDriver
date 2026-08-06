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

    private fun DesktopComposeUiTest.capture(uiState: OtherFeedbackDetailUiState = OtherFeedbackDetailUiState()) {
        setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        OtherFeedbackDetailPaneContent(uiState = uiState)
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
