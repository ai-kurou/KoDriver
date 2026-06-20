package kurou.kodriver.presentation

import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class ConnectionBannerContentScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `SIMULATOR接続中`() {
        rule.setContent {
            KoDriverTheme {
                ConnectionBannerContent(
                    uiState = ConnectionBannerUiState(
                        status = ConnectionBannerStatus.CONNECTED,
                        message = "シミュレータに接続中",
                        iconType = ConnectionBannerIconType.SIMULATOR,
                    ),
                    modifier = Modifier.requiredWidth(360.dp),
                )
            }
        }
        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `SIMULATOR接続待機中`() {
        rule.setContent {
            KoDriverTheme {
                ConnectionBannerContent(
                    uiState = ConnectionBannerUiState(
                        status = ConnectionBannerStatus.DISCONNECTED,
                        message = "シミュレータ接続待機中",
                        iconType = ConnectionBannerIconType.SIMULATOR,
                    ),
                    modifier = Modifier.requiredWidth(360.dp),
                )
            }
        }
        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `NETWORK接続中`() {
        rule.setContent {
            KoDriverTheme {
                ConnectionBannerContent(
                    uiState = ConnectionBannerUiState(
                        status = ConnectionBannerStatus.CONNECTED,
                        message = "Windows版KoDriverに接続中",
                        iconType = ConnectionBannerIconType.NETWORK,
                    ),
                    modifier = Modifier.requiredWidth(360.dp),
                )
            }
        }
        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `NETWORK接続待機中`() {
        rule.setContent {
            KoDriverTheme {
                ConnectionBannerContent(
                    uiState = ConnectionBannerUiState(
                        status = ConnectionBannerStatus.DISCONNECTED,
                        message = "Windows版KoDriver接続待機中",
                        iconType = ConnectionBannerIconType.NETWORK,
                    ),
                    modifier = Modifier.requiredWidth(360.dp),
                )
            }
        }
        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `NETWORK IPアドレス未設定`() {
        rule.setContent {
            KoDriverTheme {
                ConnectionBannerContent(
                    uiState = ConnectionBannerUiState(
                        status = ConnectionBannerStatus.DISCONNECTED,
                        message = "接続先IPアドレスが未設定です",
                        iconType = ConnectionBannerIconType.NETWORK,
                    ),
                    modifier = Modifier.requiredWidth(360.dp),
                )
            }
        }
        rule.onRoot().captureRoboImage()
    }
}
