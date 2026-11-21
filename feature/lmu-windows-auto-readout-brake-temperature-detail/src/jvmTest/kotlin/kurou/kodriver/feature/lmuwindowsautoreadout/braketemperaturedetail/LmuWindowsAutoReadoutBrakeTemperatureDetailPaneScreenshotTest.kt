package kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail

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
                        Column(modifier = Modifier.requiredSize(480.dp, 1080.dp)) {
                            BrakeTemperatureThresholdHelpSheetContent()
                        }
                    }
                }
            }
            onNodeWithText(
                "選択した車両クラスで走行中に、4輪のうち1輪でも設定した温度以上になると過熱警告を読み上げます。" +
                    "ブレーキの適正温度はディスクの素材によって大きく異なり、LMPやハイパーカーのカーボンディスクは高温側、GT3などのスチールディスクは低温側が適正です。" +
                    "初期値は全クラス共通のため、走行後にデバッグ画面のブレーキ温度を確認しながらクラスごとに調整してください。",
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
                            LmuWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutBrakeTemperatureDetailUiState(
                                        readoutText = "ブレーキ温度は{celsius}℃以上です",
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
