package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test

class PlayingEqualizerScreenshotTest {
    @Test
    fun `再生中を表示する`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(Modifier.requiredSize(64.dp, 48.dp)) {
                            PlayingEqualizer(contentDescription = "試聴を停止")
                        }
                    }
                }
            }
            onNodeWithContentDescription("試聴を停止").assertExists()
            onRoot().captureRoboImage()
        }
}
