@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherthemedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.github.takahirom.roborazzi.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.defaultRoborazziOptions
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ThemeMode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w480dp-h320dp")
class OtherThemeDetailDialogContentScreenshotTest {
    @Test
    fun `システムに従うを選択中`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OtherThemeDetailDialogContent(
                            uiState = OtherThemeDetailUiState(pendingThemeMode = ThemeMode.SYSTEM),
                        )
                    }
                }
            }
        }
    }
}
