package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

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

class Gt7Ps5ReadoutRemainingFuelLapsDetailPaneScreenshotTest {
    // 長いラベルと挿入ヒントが折り返すスマホ幅で、項目のまとまりを確認する。
    @Test
    fun `スマホ幅の文言グループ`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                                uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(isTextToSpeechAvailable = true),
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
                            Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                                uiState =
                                    Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
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
                            Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                                uiState =
                                    Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
                                        readoutText = "あと{laps}周です",
                                        emptyReadoutText = "燃料切れです",
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
