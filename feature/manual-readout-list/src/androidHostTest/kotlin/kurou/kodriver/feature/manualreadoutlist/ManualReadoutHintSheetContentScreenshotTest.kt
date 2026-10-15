@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.material3.Surface
import com.github.takahirom.roborazzi.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.defaultRoborazziOptions
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w480dp-h640dp")
class ManualReadoutHintSheetContentScreenshotTest {
    @Test
    fun `Android版ではホットキーの説明を表示しない`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            KoDriverTheme {
                Surface {
                    ManualReadoutHintSheetContent(showHotkeyHint = isManualReadoutHotkeySupported())
                }
            }
        }
    }
}
