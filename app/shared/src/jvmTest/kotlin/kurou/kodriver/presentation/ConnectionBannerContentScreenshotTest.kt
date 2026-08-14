package kurou.kodriver.presentation

import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test

class ConnectionBannerContentScreenshotTest {
    @Test
    fun `SIMULATOR接続中`() =
        composeScreenshotTest {
            setThemedContent {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.CONNECTED,
                            message = "シミュレータに接続中",
                            iconType = ConnectionBannerIconType.SIMULATOR,
                        ),
                    modifier = Modifier.requiredWidth(360.dp),
                )
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `SIMULATOR接続待機中`() =
        composeScreenshotTest {
            setThemedContent {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.DISCONNECTED,
                            message = "シミュレータ接続待機中",
                            iconType = ConnectionBannerIconType.SIMULATOR,
                        ),
                    modifier = Modifier.requiredWidth(360.dp),
                )
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `NETWORK接続中`() =
        composeScreenshotTest {
            setThemedContent {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.CONNECTED,
                            message = "Windows版KoDriverに接続中",
                            iconType = ConnectionBannerIconType.NETWORK,
                        ),
                    modifier = Modifier.requiredWidth(360.dp),
                )
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `NETWORK接続待機中`() =
        composeScreenshotTest {
            setThemedContent {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.DISCONNECTED,
                            message = "Windows版KoDriver接続待機中",
                            iconType = ConnectionBannerIconType.NETWORK,
                        ),
                    modifier = Modifier.requiredWidth(360.dp),
                )
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `NETWORK IPアドレス未設定`() =
        composeScreenshotTest {
            setThemedContent {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.DISCONNECTED,
                            message = "接続先IPアドレスが未設定です",
                            iconType = ConnectionBannerIconType.NETWORK,
                        ),
                    modifier = Modifier.requiredWidth(360.dp),
                )
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `NETWORK IPアドレス未設定 タップ可能`() =
        composeScreenshotTest {
            setThemedContent {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.UNCHECKED,
                            message = "接続先IPアドレスが未設定です",
                            iconType = ConnectionBannerIconType.NETWORK,
                            isTappable = true,
                        ),
                    modifier = Modifier.requiredWidth(360.dp),
                    onClick = {},
                )
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `SIMULATOR接続待機中 ダークテーマ`() =
        composeScreenshotTest {
            setThemedContent(darkTheme = true) {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.DISCONNECTED,
                            message = "シミュレータ接続待機中",
                            iconType = ConnectionBannerIconType.SIMULATOR,
                        ),
                    modifier = Modifier.requiredWidth(360.dp),
                )
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `SIMULATOR接続中 ダークテーマ`() =
        composeScreenshotTest {
            setThemedContent(darkTheme = true) {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.CONNECTED,
                            message = "シミュレータに接続中",
                            iconType = ConnectionBannerIconType.SIMULATOR,
                        ),
                    modifier = Modifier.requiredWidth(360.dp),
                )
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `NETWORK IPアドレス未設定 タップ可能 ダークテーマ`() =
        composeScreenshotTest {
            setThemedContent(darkTheme = true) {
                ConnectionBannerContent(
                    uiState =
                        ConnectionBannerUiState(
                            status = ConnectionBannerStatus.UNCHECKED,
                            message = "接続先IPアドレスが未設定です",
                            iconType = ConnectionBannerIconType.NETWORK,
                            isTappable = true,
                        ),
                    modifier = Modifier.requiredWidth(360.dp),
                    onClick = {},
                )
            }
            onRoot().captureRoboImage()
        }

    private fun DesktopComposeUiTest.setThemedContent(
        darkTheme: Boolean = false,
        content: @Composable () -> Unit,
    ) {
        setContent {
            AppTheme(darkTheme = darkTheme) {
                Surface(content = content)
            }
        }
    }
}
