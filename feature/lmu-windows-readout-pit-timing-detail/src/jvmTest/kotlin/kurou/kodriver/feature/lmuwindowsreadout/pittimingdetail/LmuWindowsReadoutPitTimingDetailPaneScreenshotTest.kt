package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

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

class LmuWindowsReadoutPitTimingDetailPaneScreenshotTest {
    // 長いラベルと挿入ヒントが折り返すスマホ幅で、項目のまとまりを確認する。
    @Test
    fun `スマホ幅の文言グループ`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            LmuWindowsReadoutPitTimingDetailPaneContent(
                                uiState = LmuWindowsReadoutPitTimingDetailUiState(isTextToSpeechAvailable = true),
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
                            LmuWindowsReadoutPitTimingDetailPaneContent(
                                uiState = LmuWindowsReadoutPitTimingDetailUiState(isTextToSpeechAvailable = true),
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `予想残り周回数のヘルプボトムシート`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            PitTimingLapsHelpSheetContent()
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `通常入力あり`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsReadoutPitTimingDetailPaneContent(
                                uiState =
                                    LmuWindowsReadoutPitTimingDetailUiState(
                                        virtualEnergyText = "残り{laps}周です",
                                        tyreWearText = "残り{laps}周です",
                                        virtualEnergyImminentText = "今すぐピットへ",
                                        tyreWearImminentText = "今すぐピットへ",
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
    fun `編集済み文言のリセットボタン表示`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsReadoutPitTimingDetailPaneContent(
                                uiState =
                                    LmuWindowsReadoutPitTimingDetailUiState(
                                        virtualEnergyText = "編集済みの通常文言",
                                        tyreWearText = "編集済みの通常文言",
                                        virtualEnergyImminentText = "編集済みの必須文言",
                                        tyreWearImminentText = "編集済みの必須文言",
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
    fun `空欄`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsReadoutPitTimingDetailPaneContent(
                                uiState =
                                    LmuWindowsReadoutPitTimingDetailUiState(
                                        virtualEnergyText = "",
                                        tyreWearText = "",
                                        virtualEnergyImminentText = "",
                                        tyreWearImminentText = "",
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
    fun `未知トークン警告`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsReadoutPitTimingDetailPaneContent(
                                uiState =
                                    LmuWindowsReadoutPitTimingDetailUiState(
                                        virtualEnergyText = "{lap}周{x}",
                                        tyreWearText = "{lap}周{x}",
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
                            LmuWindowsReadoutPitTimingDetailPaneContent(
                                uiState = LmuWindowsReadoutPitTimingDetailUiState(isTextToSpeechAvailable = false),
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }
}
