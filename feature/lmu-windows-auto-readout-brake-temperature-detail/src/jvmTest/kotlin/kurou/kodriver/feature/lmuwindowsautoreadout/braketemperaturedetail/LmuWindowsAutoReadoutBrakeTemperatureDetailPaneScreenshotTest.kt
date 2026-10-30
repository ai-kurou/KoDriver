package kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsAllVehicleClasses
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault
import org.junit.Test

class LmuWindowsAutoReadoutBrakeTemperatureDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutBrakeTemperatureDetailUiState(
                                        isTextToSpeechAvailable = true,
                                        vehicleClassHighThresholdCelsius =
                                            lmuWindowsAllVehicleClasses.associateWith { vehicleClass ->
                                                lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(
                                                    vehicleClass,
                                                )
                                            },
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
                        Box(modifier = Modifier.requiredSize(480.dp, 1080.dp)) {
                            BrakeTemperatureThresholdHelpSheetContent()
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `編集済み文言のリセットボタン表示`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutBrakeTemperatureDetailUiState(
                                        readoutText = "ブレーキ温度{celsius}℃以上です",
                                        isTextToSpeechAvailable = true,
                                    ),
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }
}
