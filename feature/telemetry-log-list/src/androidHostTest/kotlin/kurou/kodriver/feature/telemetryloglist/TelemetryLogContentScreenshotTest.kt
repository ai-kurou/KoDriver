@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.telemetryloglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.ui.Modifier
import com.github.takahirom.roborazzi.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.defaultRoborazziOptions
import kurou.kodriver.buildlogic.screenshottest.rememberFoldedVerticalHingeDirective
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w840dp-h640dp")
class TelemetryLogContentScreenshotTest {
    @Test
    fun `折りたたみ端末の縦ヒンジを避けて2ペイン表示`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        TelemetryLogContentScaffold(
                            uiState = previewTelemetryLogListUiState.copy(selectedLogId = 2),
                            scaffoldDirective = rememberFoldedVerticalHingeDirective(),
                            detailContent = { logId -> Text("Detail: $logId") },
                        )
                    }
                }
            }
        }
    }
}
