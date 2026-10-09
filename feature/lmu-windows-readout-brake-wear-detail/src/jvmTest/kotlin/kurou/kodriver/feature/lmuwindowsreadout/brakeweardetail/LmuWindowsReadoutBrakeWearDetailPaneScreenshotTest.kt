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
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearWheelRemaining
import kurou.kodriver.domain.model.WheelIndex
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
    fun `4輪の残量を表示する`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsReadoutBrakeWearDetailPaneContent(
                                uiState =
                                    LmuWindowsReadoutBrakeWearDetailUiState(
                                        remaining =
                                            LmuWindowsBrakeWearRemainingData(
                                                wheels =
                                                    mapOf(
                                                        WheelIndex.FRONT_LEFT to wheel(0.0305f, 50),
                                                        WheelIndex.FRONT_RIGHT to wheel(0.0310f, 55),
                                                        WheelIndex.REAR_LEFT to wheel(0.0330f, 73),
                                                        WheelIndex.REAR_RIGHT to wheel(0.0335f, 77),
                                                    ),
                                            ),
                                    ),
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    private fun wheel(
        thickness: Float,
        percent: Int,
    ) = LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(thickness), percent)
}
