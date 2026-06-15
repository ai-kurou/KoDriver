package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class OtherServerIpDetailDialogScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        OtherServerIpDetailDialogContent(
                            uiState = OtherServerIpDetailUiState(inputIp = "192.168.1.100"),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(1).captureRoboImage()
    }

    @Test
    fun `不正なIPアドレス`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        OtherServerIpDetailDialogContent(
                            uiState = OtherServerIpDetailUiState(inputIp = "invalid", isInputValid = false),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(1).captureRoboImage()
    }

    @Test
    fun `空入力`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        OtherServerIpDetailDialogContent(
                            uiState = OtherServerIpDetailUiState(inputIp = ""),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(1).captureRoboImage()
    }

    @Test
    fun `接続確認中`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        OtherServerIpDetailDialogContent(
                            uiState = OtherServerIpDetailUiState(
                                inputIp = "192.168.1.100",
                                isCheckingConnectivity = true,
                            ),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(1).captureRoboImage()
    }

    @Test
    fun `接続警告あり`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        OtherServerIpDetailDialogContent(
                            uiState = OtherServerIpDetailUiState(
                                inputIp = "192.168.1.100",
                                connectivityWarning = true,
                            ),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(1).captureRoboImage()
    }
}
