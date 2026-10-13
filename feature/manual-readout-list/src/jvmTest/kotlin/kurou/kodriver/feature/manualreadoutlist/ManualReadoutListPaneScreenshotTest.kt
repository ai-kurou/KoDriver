package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class ManualReadoutListPaneScreenshotTest {
    @Test
    fun `空状態`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                            ManualReadoutListPane()
                        }
                    }
                }
            }

            onRoot().captureRoboImage()
        }

    @Test
    fun `ヘルプボトムシートホットキーあり`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Column(modifier = Modifier.requiredSize(480.dp, 400.dp)) {
                            ManualReadoutHintSheetContent(showHotkeyHint = true)
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `ヘルプボトムシートホットキーなし`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Column(modifier = Modifier.requiredSize(480.dp, 400.dp)) {
                            ManualReadoutHintSheetContent(showHotkeyHint = false)
                        }
                    }
                }
            }
            onRoot().captureRoboImage()
        }
}
