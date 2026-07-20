@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w480dp-h360dp")
class VersionMismatchBottomSheetScreenshotTest {

    @Test
    fun `Windows版が新しい`() {
        capture(windowsKoDriverVersion = "2.0.0", appVersion = "1.0.0")
    }

    @Test
    fun `Android版が新しい`() {
        capture(windowsKoDriverVersion = "1.0.0", appVersion = "2.0.0")
    }

    private fun capture(windowsKoDriverVersion: String, appVersion: String) {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            AppTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        VersionMismatchBottomSheetContent(
                            windowsKoDriverVersion = windowsKoDriverVersion,
                            appVersion = appVersion,
                            onDismiss = {},
                        )
                    }
                }
            }
        }
    }
}
