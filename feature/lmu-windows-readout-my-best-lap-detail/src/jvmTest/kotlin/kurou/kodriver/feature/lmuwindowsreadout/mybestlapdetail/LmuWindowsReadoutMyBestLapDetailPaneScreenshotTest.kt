package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

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
import kurou.kodriver.domain.model.MyBestLapVoiceType
import org.junit.Test

class LmuWindowsReadoutMyBestLapDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            capturePane()
        }

    @Test
    fun `casual選択`() =
        composeScreenshotTest {
            capturePane(
                uiState =
                    LmuWindowsReadoutMyBestLapDetailUiState(
                        voiceType = MyBestLapVoiceType.CASUAL,
                    ),
            )
        }

    private fun DesktopComposeUiTest.capturePane(
        uiState: LmuWindowsReadoutMyBestLapDetailUiState = LmuWindowsReadoutMyBestLapDetailUiState(),
    ) {
        setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        LmuWindowsReadoutMyBestLapDetailPaneContent(uiState = uiState)
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
