package kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail

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
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsAllVehicleClasses
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault
import org.junit.Test

class LmuWindowsAutoReadoutBrakeWearDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutBrakeWearDetailUiState(
                                        isTextToSpeechAvailable = true,
                                        vehicleClassLowThresholdPercent =
                                            lmuWindowsAllVehicleClasses.associateWith { vehicleClass ->
                                                lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(
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
                        Column(modifier = Modifier.requiredSize(480.dp, 1080.dp)) {
                            BrakeWearThresholdHelpSheetContent()
                        }
                    }
                }
            }
            onNodeWithText(
                "選択した車両クラスで走行中に、4輪のうち1輪でも残量が設定した%以下になると警告を読み上げます。" +
                    "残量は、観測した最大の厚さ（新品時とみなします）から、車両クラスごとの破損厚さまでの間でどこにあるかを表します。初期値は全クラス共通のため、走行しながらクラスごとに調整してください。",
            ).assertIsDisplayed()
            onRoot().captureRoboImage()
        }

    @Test
    fun `編集済み文言のリセットボタン表示`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutBrakeWearDetailUiState(
                                        readoutText = "ブレーキは残り{percent}%以下です",
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
