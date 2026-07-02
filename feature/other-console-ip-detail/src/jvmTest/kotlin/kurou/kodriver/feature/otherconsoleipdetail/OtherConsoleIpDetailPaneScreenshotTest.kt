package kurou.kodriver.feature.otherconsoleipdetail

import androidx.compose.material3.Surface
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class OtherConsoleIpDetailPaneScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    OtherConsoleIpDetailPaneContent(
                        uiState = OtherConsoleIpDetailUiState(inputAddress = "192.168.1.100"),
                    )
                }
            }
        }
        rule.onAllNodes(isRoot()).get(0).captureRoboImage()
    }

    @Test
    fun `空入力`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    OtherConsoleIpDetailPaneContent(
                        uiState = OtherConsoleIpDetailUiState(inputAddress = ""),
                    )
                }
            }
        }
        rule.onAllNodes(isRoot()).get(0).captureRoboImage()
    }

    @Test
    fun `不正なIPアドレス`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    OtherConsoleIpDetailPaneContent(
                        uiState = OtherConsoleIpDetailUiState(inputAddress = "invalid", isInputValid = false),
                    )
                }
            }
        }
        rule.onAllNodes(isRoot()).get(0).captureRoboImage()
    }

    @Test
    fun `保存失敗`() {
        rule.setContent {
            KoDriverTheme {
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
        rule.onAllNodes(isRoot()).get(0).captureRoboImage()
    }
}
