@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.telemetryloglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class TelemetryLogResetConfirmDialogScreenshotTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                        TelemetryLogResetConfirmDialog(
                            onConfirm = {},
                            onDismiss = {},
                        )
                    }
                }
            }
        }
        rule.onNode(isDialog()).captureRoboImage()
    }
}
