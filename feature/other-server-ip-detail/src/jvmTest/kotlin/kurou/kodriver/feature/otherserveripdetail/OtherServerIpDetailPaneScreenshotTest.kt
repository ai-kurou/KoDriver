package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class OtherServerIpDetailPaneScreenshotTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        OtherServerIpDetailPaneContent(
                            uiState = OtherServerIpDetailUiState(inputIp = "192.168.1.100"),
                        )
                    }
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
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        OtherServerIpDetailPaneContent(
                            uiState = OtherServerIpDetailUiState(inputIp = "invalid", isInputValid = false),
                        )
                    }
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
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        OtherServerIpDetailPaneContent(
                            uiState = OtherServerIpDetailUiState(inputIp = ""),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(0).captureRoboImage()
    }

    @Test
    fun `接続確認中`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        OtherServerIpDetailPaneContent(
                            uiState =
                                OtherServerIpDetailUiState(
                                    inputIp = "192.168.1.100",
                                    isCheckingConnectivity = true,
                                ),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(0).captureRoboImage()
    }

    @Test
    fun `接続警告あり`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        OtherServerIpDetailPaneContent(
                            uiState =
                                OtherServerIpDetailUiState(
                                    inputIp = "192.168.1.100",
                                    connectivityWarning = true,
                                ),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(0).captureRoboImage()
    }

    @Test
    fun `検出中表示`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        OtherServerIpDetailPaneContent(
                            uiState =
                                OtherServerIpDetailUiState(
                                    inputIp = "192.168.1.100",
                                    discoveredServers = emptyList(),
                                ),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(0).captureRoboImage()
    }

    @Test
    fun `見つかったWindows版KoDriverを選択ボタン表示`() {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 640.dp)) {
                        OtherServerIpDetailPaneContent(
                            uiState =
                                OtherServerIpDetailUiState(
                                    inputIp = "192.168.1.100",
                                    discoveredServers =
                                        listOf(
                                            DiscoveredServer(hostName = "DESKTOP-ABC123", ipAddress = "192.168.1.10"),
                                        ),
                                    isDiscoveryDialogVisible = false,
                                ),
                        )
                    }
                }
            }
        }
        rule.onAllNodes(isRoot()).get(0).captureRoboImage()
    }
}
