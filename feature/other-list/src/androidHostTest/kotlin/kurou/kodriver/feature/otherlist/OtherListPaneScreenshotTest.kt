@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h640dp")
class OtherListPaneScreenshotTest {

    @Test
    fun `デフォルト`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OtherListPane(
                            uiState = OtherListUiState(),
                            onItemClick = {},
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `アップデートバッジを表示`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OtherListPane(
                            uiState = OtherListUiState(hasAppUpdate = true),
                            onItemClick = {},
                        )
                    }
                }
            }
        }
    }
}
