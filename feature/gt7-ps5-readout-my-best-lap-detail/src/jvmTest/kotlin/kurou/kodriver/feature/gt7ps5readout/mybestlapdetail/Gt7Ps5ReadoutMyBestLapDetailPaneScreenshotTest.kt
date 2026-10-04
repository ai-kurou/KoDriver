package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

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

class Gt7Ps5ReadoutMyBestLapDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            Gt7Ps5ReadoutMyBestLapDetailPaneContent(
                                uiState =
                                    Gt7Ps5ReadoutMyBestLapDetailUiState(
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
                            Gt7Ps5ReadoutMyBestLapDetailPaneContent(
                                uiState =
                                    Gt7Ps5ReadoutMyBestLapDetailUiState(
                                        readoutText = "更新{laptime}です",
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
                            Gt7Ps5ReadoutMyBestLapDetailPaneContent(
                                uiState =
                                    Gt7Ps5ReadoutMyBestLapDetailUiState(
                                        readoutText = " ",
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
                            Gt7Ps5ReadoutMyBestLapDetailPaneContent(
                                uiState = Gt7Ps5ReadoutMyBestLapDetailUiState(readoutText = "{unknown}"),
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `未知プレースホルダー`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            Gt7Ps5ReadoutMyBestLapDetailPaneContent(
                                uiState =
                                    Gt7Ps5ReadoutMyBestLapDetailUiState(
                                        readoutText = "{wheel} {laptime}",
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
    fun `警告OFF時`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            Gt7Ps5ReadoutMyBestLapDetailPaneContent(
                                uiState =
                                    Gt7Ps5ReadoutMyBestLapDetailUiState(
                                        enabled = false,
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
