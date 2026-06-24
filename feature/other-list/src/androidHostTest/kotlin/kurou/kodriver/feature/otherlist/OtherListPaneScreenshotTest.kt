@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OtherListPaneScreenshotTest {

    @Test
    fun `デフォルト`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
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
                    Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
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
