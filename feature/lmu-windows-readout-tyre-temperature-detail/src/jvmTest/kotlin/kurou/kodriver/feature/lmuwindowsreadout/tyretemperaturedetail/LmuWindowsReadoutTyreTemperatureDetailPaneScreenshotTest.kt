package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.lmuWindowsAllVehicleClasses
import kurou.kodriver.domain.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import org.junit.Test

class LmuWindowsReadoutTyreTemperatureDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsReadoutTyreTemperatureDetailUiState(
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
                            LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsReadoutTyreTemperatureDetailUiState(
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
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            TyreTemperatureThresholdHelpSheetContent()
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `低温警告対象フェーズのヘルプボトムシート`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            TyreTemperatureLowWarningPhasesHelpSheetContent()
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `空白文言`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsReadoutTyreTemperatureDetailUiState(
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
                            LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsReadoutTyreTemperatureDetailUiState(
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
                            LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsReadoutTyreTemperatureDetailUiState(
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
                            LmuWindowsReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    LmuWindowsReadoutTyreTemperatureDetailUiState(
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
