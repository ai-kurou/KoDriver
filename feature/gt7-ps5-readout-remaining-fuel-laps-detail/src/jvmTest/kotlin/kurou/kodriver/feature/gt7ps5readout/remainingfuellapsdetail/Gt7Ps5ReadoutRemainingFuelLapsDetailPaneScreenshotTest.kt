package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

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
import org.junit.Test

class Gt7Ps5ReadoutRemainingFuelLapsDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            capturePane()
        }

    private fun DesktopComposeUiTest.capturePane(
        uiState: Gt7Ps5ReadoutRemainingFuelLapsDetailUiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(),
    ) {
        setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                        Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(uiState = uiState)
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
