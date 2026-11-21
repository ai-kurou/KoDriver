package kurou.kodriver.feature.lmuwindowsautoreadout.vehicleapproachdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class LmuWindowsAutoReadoutVehicleApproachDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsAutoReadoutVehicleApproachDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutVehicleApproachDetailUiState(
                                        isTextToSpeechAvailable = true,
                                    ),
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
                            LmuWindowsAutoReadoutVehicleApproachDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutVehicleApproachDetailUiState(
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
    fun `編集済み文言のリセットボタン表示`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsAutoReadoutVehicleApproachDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutVehicleApproachDetailUiState(
                                        isTextToSpeechAvailable = true,
                                        startLeftText = "左注意",
                                        startRightText = "右注意",
                                        sustainedLeftText = "左継続",
                                        sustainedRightText = "右継続",
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
                            LmuWindowsAutoReadoutVehicleApproachDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutVehicleApproachDetailUiState(
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
                        Column(modifier = Modifier.requiredSize(480.dp, 1080.dp)) {
                            VehicleApproachHelpSheetContent()
                        }
                    }
                }
            }
            onNodeWithText(
                "閾値は自車中心から相手車両中心までの距離です。この範囲内に入ると車両接近と判定され読み上げられます。\n\n※車両は一般的に全長 5m以下、全幅 2m以下となっています。",
            ).assertIsDisplayed()
            onRoot().captureRoboImage()
        }
}
