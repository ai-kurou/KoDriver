@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.telemetryloglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class TelemetryLogDeleteConfirmDialogScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                            TelemetryLogDeleteConfirmDialog(
                                onConfirm = {},
                                onDismiss = {},
                            )
                        }
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }

    @Test
    fun `デフォルト ダークテーマ`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme(darkTheme = true) {
                    Surface {
                        Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                            TelemetryLogDeleteConfirmDialog(
                                onConfirm = {},
                                onDismiss = {},
                            )
                        }
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }
}
