package kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail

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
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import org.junit.Test

class LmuWindowsAutoReadoutTyreTemperatureDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                                        isTextToSpeechAvailable = true,
                                        vehicleClassHighThresholdCelsius =
                                            lmuWindowsAllVehicleClasses.associateWith { vehicleClass ->
                                                lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(
                                                    vehicleClass,
                                                ).value
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
    fun `警告OFF時`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                                        isTextToSpeechAvailable = true,
                                        overheatWarningEnabled = false,
                                        lowWarningEnabled = false,
                                        vehicleClassHighThresholdCelsius =
                                            lmuWindowsAllVehicleClasses.associateWith { vehicleClass ->
                                                lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(
                                                    vehicleClass,
                                                ).value
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
                            TyreTemperatureThresholdHelpSheetContent()
                        }
                    }
                }
            }
            onNodeWithText(
                "設定した温度以上になると過熱警告を読み上げます。一度警告を読み上げた後は、閾値より約5℃低い温度まで下がるまで再度読み上げません。温度が閾値付近で細かく上下しても連続して警告しないための制御です。",
            ).assertIsDisplayed()
            onRoot().captureRoboImage()
        }

    @Test
    fun `低温警告対象フェーズのヘルプボトムシート`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Column(modifier = Modifier.requiredSize(480.dp, 1080.dp)) {
                            TyreTemperatureLowWarningPhasesHelpSheetContent()
                        }
                    }
                }
            }
            onNodeWithText(
                "ガレージ・ウォームアップラップ・グリッドウォーク・フォーメーションラップのうち、選択したフェーズに切り替わった瞬間に、4輪のうち1輪でも60℃以下であれば低温警告を読み上げます。" +
                    "フェーズが変わらない間や、切り替わった時点で全輪が60℃を超えている場合は読み上げません。",
            ).assertIsDisplayed()
            onRoot().captureRoboImage()
        }

    @Test
    fun `空白文言`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                                        isTextToSpeechAvailable = true,
                                        overheatReadoutText = "",
                                        coldReadoutText = "",
                                        vehicleClassHighThresholdCelsius =
                                            lmuWindowsAllVehicleClasses.associateWith { vehicleClass ->
                                                lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(
                                                    vehicleClass,
                                                ).value
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
    fun `TTS利用不可`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                                        isTextToSpeechAvailable = false,
                                        vehicleClassHighThresholdCelsius =
                                            lmuWindowsAllVehicleClasses.associateWith { vehicleClass ->
                                                lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(
                                                    vehicleClass,
                                                ).value
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
    fun `低温のみ空白文言`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                                        isTextToSpeechAvailable = true,
                                        coldReadoutText = "",
                                        vehicleClassHighThresholdCelsius =
                                            lmuWindowsAllVehicleClasses.associateWith { vehicleClass ->
                                                lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(
                                                    vehicleClass,
                                                ).value
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
    fun `過熱のみ空白文言`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutTyreTemperatureDetailUiState(
                                        isTextToSpeechAvailable = true,
                                        overheatReadoutText = "",
                                        vehicleClassHighThresholdCelsius =
                                            lmuWindowsAllVehicleClasses.associateWith { vehicleClass ->
                                                lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(
                                                    vehicleClass,
                                                ).value
                                            },
                                    ),
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }
}
