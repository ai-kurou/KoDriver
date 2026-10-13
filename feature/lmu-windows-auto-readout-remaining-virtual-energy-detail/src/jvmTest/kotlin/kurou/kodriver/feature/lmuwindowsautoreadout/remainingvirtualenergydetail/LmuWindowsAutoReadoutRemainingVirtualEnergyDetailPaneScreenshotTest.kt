package kurou.kodriver.feature.lmuwindowsautoreadout.remainingvirtualenergydetail

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

class LmuWindowsAutoReadoutRemainingVirtualEnergyDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            LmuWindowsAutoReadoutRemainingVirtualEnergyDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutRemainingVirtualEnergyDetailUiState(
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
                            LmuWindowsAutoReadoutRemainingVirtualEnergyDetailPaneContent(
                                uiState =
                                    LmuWindowsAutoReadoutRemainingVirtualEnergyDetailUiState(
                                        readoutText = "エナジー残量{percent}%以下です",
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
