@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import org.junit.Test

class VersionMismatchBottomSheetScreenshotTest {
    @Test
    fun `Windows版が新しい`() =
        composeScreenshotTest {
            capture(windowsKoDriverVersion = "2.0.0", appVersion = "1.0.0")
        }

    @Test
    fun `Android版が新しい`() =
        composeScreenshotTest {
            capture(windowsKoDriverVersion = "1.0.0", appVersion = "2.0.0")
        }

    private fun DesktopComposeUiTest.capture(
        windowsKoDriverVersion: String,
        appVersion: String,
    ) {
        setContent {
            AppTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(width = 480.dp, height = 360.dp)) {
                        VersionMismatchBottomSheetContent(
                            windowsKoDriverVersion = windowsKoDriverVersion,
                            appVersion = appVersion,
                            onDismiss = {},
                        )
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
