@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherconsoleipdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OtherConsoleIpDetailPaneScreenshotTest {

    @Test
    fun `デフォルト`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    OtherConsoleIpDetailPaneContent(
                        uiState = OtherConsoleIpDetailUiState(inputAddress = "192.168.1.100"),
                    )
                }
            }
        }
    }

    @Test
    fun `空入力`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    OtherConsoleIpDetailPaneContent(
                        uiState = OtherConsoleIpDetailUiState(inputAddress = ""),
                    )
                }
            }
        }
    }

    @Test
    fun `不正なIPアドレス`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    OtherConsoleIpDetailPaneContent(
                        uiState = OtherConsoleIpDetailUiState(inputAddress = "invalid", isInputValid = false),
                    )
                }
            }
        }
    }

    @Test
    fun `保存失敗`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    OtherConsoleIpDetailPaneContent(
                        uiState = OtherConsoleIpDetailUiState(
                            inputAddress = "192.168.1.100",
                            saveFailed = true,
                        ),
                    )
                }
            }
        }
    }
}
