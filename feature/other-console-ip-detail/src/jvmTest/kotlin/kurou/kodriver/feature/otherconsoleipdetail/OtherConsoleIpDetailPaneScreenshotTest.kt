package kurou.kodriver.feature.otherconsoleipdetail

import androidx.compose.material3.Surface
import androidx.compose.ui.test.isRoot
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class OtherConsoleIpDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        OtherConsoleIpDetailPaneContent(
                            uiState = OtherConsoleIpDetailUiState(inputAddress = "192.168.1.100"),
                        )
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `空入力`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        OtherConsoleIpDetailPaneContent(
                            uiState = OtherConsoleIpDetailUiState(inputAddress = ""),
                        )
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `不正なIPアドレス`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        OtherConsoleIpDetailPaneContent(
                            uiState = OtherConsoleIpDetailUiState(inputAddress = "invalid", isInputValid = false),
                        )
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `保存失敗`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        OtherConsoleIpDetailPaneContent(
                            uiState =
                                OtherConsoleIpDetailUiState(
                                    inputAddress = "192.168.1.100",
                                    saveFailed = true,
                                ),
                        )
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }
}
