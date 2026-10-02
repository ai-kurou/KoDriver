package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class LmuWindowsReadoutVehicleApproachDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsReadoutVehicleApproachDetailPaneContent(
                                uiState = LmuWindowsReadoutVehicleApproachDetailUiState(isTextToSpeechAvailable = true),
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `カスタム文言と空欄`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsReadoutVehicleApproachDetailPaneContent(
                                uiState =
                                    LmuWindowsReadoutVehicleApproachDetailUiState(
                                        isTextToSpeechAvailable = true,
                                        startLeftText = "左注意",
                                        startRightText = "",
                                    ),
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `TTS利用不可`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsReadoutVehicleApproachDetailPaneContent(
                                uiState =
                                    LmuWindowsReadoutVehicleApproachDetailUiState(
                                        isTextToSpeechAvailable = false,
                                    ),
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `ヘルプボトムシート`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            VehicleApproachHelpSheetContent()
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }
}
