package kurou.kodriver.feature.otherconsoleipdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test

class OtherConsoleIpDetailPaneScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
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
            MaterialTheme(colorScheme = lightColorScheme()) {
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
            MaterialTheme(colorScheme = lightColorScheme()) {
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
        rule.onAllNodes(isRoot()).get(0).captureRoboImage()
    }

    @Test
    fun `ポート選択不可`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    OtherConsoleIpDetailPaneContent(
                        uiState = OtherConsoleIpDetailUiState(inputAddress = "192.168.1.100"),
                        portSelectable = false,
                    )
                }
            }
        }
        rule.onAllNodes(isRoot()).get(0).captureRoboImage()
    }
}
