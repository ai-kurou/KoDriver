package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class Gt7Ps5ReadoutRemainingFuelLapsDetailPaneScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        capturePane()
    }

    @Test
    fun `1周設定`() {
        capturePane(uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(remainingFuelLaps = 1))
    }

    private fun capturePane(
        uiState: Gt7Ps5ReadoutRemainingFuelLapsDetailUiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(),
    ) {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(uiState = uiState)
                    }
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }
}
