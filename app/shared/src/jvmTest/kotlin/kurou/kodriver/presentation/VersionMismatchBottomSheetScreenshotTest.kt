@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class VersionMismatchBottomSheetScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `Windows版が新しい`() {
        capture(windowsKoDriverVersion = "2.0.0", appVersion = "1.0.0")
    }

    @Test
    fun `Android版が新しい`() {
        capture(windowsKoDriverVersion = "1.0.0", appVersion = "2.0.0")
    }

    private fun capture(windowsKoDriverVersion: String, appVersion: String) {
        rule.setContent {
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
        rule.onRoot().captureRoboImage()
    }
}
