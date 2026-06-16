package kurou.kodriver.feature.lmureadout.vehicleapproachdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class LmuReadoutVehicleApproachDetailPaneScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        LmuReadoutVehicleApproachDetailPaneContent(uiState = LmuReadoutVehicleApproachDetailUiState())
                    }
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `ヘルプボトムシート表示`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        LmuReadoutVehicleApproachDetailPaneContent(uiState = LmuReadoutVehicleApproachDetailUiState())
                    }
                }
            }
        }
        rule.onNodeWithTag("vehicle_approach_help_button").performClick()
        rule.waitForIdle()
        rule.onRoot().captureRoboImage()
    }
}
