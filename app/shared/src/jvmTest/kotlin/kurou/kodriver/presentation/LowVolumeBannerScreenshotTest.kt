package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.feature.main.LowVolumeWarningUiState
import org.junit.Test

class LowVolumeBannerScreenshotTest {
    @Test
    fun `警告3パターン ライトテーマ`() = composeScreenshotTest { captureBanners(darkTheme = false) }

    @Test
    fun `警告3パターン ダークテーマ`() = composeScreenshotTest { captureBanners(darkTheme = true) }

    private fun DesktopComposeUiTest.captureBanners(darkTheme: Boolean) {
        setContent {
            AppTheme(darkTheme = darkTheme) {
                Surface {
                    Column(modifier = Modifier.requiredWidth(360.dp)) {
                        LowVolumeBanner(LowVolumeWarningUiState(deviceVolume = 20), {}, {})
                        LowVolumeBanner(LowVolumeWarningUiState(soundVolume = 20), {}, {})
                        LowVolumeBanner(LowVolumeWarningUiState(deviceVolume = 20, soundVolume = 20), {}, {})
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
