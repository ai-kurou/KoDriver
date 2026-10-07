package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

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

class AceWindowsReadoutTyreTemperatureDetailPaneScreenshotTest {
    // スマホ幅で文言欄・挿入チップ・温度説明の配置を確認する。
    @Test
    fun `スマホ幅のチップ行`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            AceWindowsReadoutTyreTemperatureDetailPaneContent(
                                uiState = AceWindowsReadoutTyreTemperatureDetailUiState(isTextToSpeechAvailable = true),
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            AceWindowsReadoutTyreTemperatureDetailPaneContent(
                                uiState = AceWindowsReadoutTyreTemperatureDetailUiState(isTextToSpeechAvailable = true),
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
                            AceWindowsReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    AceWindowsReadoutTyreTemperatureDetailUiState(
                                        overheatWarningEnabled = false,
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
    fun `空白文言`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            AceWindowsReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    AceWindowsReadoutTyreTemperatureDetailUiState(
                                        overheatReadoutText = "",
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
    fun `TTS利用不可`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            AceWindowsReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    AceWindowsReadoutTyreTemperatureDetailUiState(
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
    fun `未知のプレースホルダー`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            AceWindowsReadoutTyreTemperatureDetailPaneContent(
                                uiState =
                                    AceWindowsReadoutTyreTemperatureDetailUiState(
                                        overheatReadoutText = "{wheel}",
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
