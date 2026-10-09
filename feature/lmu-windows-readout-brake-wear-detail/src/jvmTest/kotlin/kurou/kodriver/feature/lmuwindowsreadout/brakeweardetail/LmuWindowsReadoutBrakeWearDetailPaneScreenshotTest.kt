package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData
import org.junit.Test

class LmuWindowsReadoutBrakeWearDetailPaneScreenshotTest {
    @Test
    fun `値を取得できていない`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsReadoutBrakeWearDetailPaneContent()
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `基準との差を表示する`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsReadoutBrakeWearDetailPaneContent(
                                uiState =
                                    LmuWindowsReadoutBrakeWearDetailUiState(
                                        current =
                                            LmuWindowsBrakeWearInvestigationData(
                                                wearablesBrakes = listOf(0.034, 0.033, 0.03, 0.029),
                                                brakeInfo = listOf(0.036, 0.036, 0.032, 0.032),
                                            ),
                                        baseline =
                                            LmuWindowsBrakeWearInvestigationData(
                                                wearablesBrakes = listOf(0.036, 0.035, 0.032, 0.031),
                                                brakeInfo = listOf(0.036, 0.036, 0.032, 0.032),
                                            ),
                                    ),
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }
}
