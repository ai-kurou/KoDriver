package kurou.kodriver.feature.othervolumedetail

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

class OtherVolumeDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() = capturePane(isPreviewing = false)

    @Test
    fun `試聴中`() = capturePane(isPreviewing = true)

    private fun capturePane(isPreviewing: Boolean) =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            OtherVolumeDetailPaneContent(
                                uiState =
                                    OtherVolumeDetailUiState(
                                        volume = 80,
                                        deviceVolume = 60,
                                        isPreviewing = isPreviewing,
                                    ),
                            )
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }
}
