package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class OtherServerIpDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            mainClock.autoAdvance = false
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            OtherServerIpDetailPaneContent(
                                uiState = OtherServerIpDetailUiState(inputIp = "192.168.1.100"),
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `不正なIPアドレス`() =
        composeScreenshotTest {
            mainClock.autoAdvance = false
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            OtherServerIpDetailPaneContent(
                                uiState = OtherServerIpDetailUiState(inputIp = "invalid", isInputValid = false),
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `空入力`() =
        composeScreenshotTest {
            mainClock.autoAdvance = false
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            OtherServerIpDetailPaneContent(
                                uiState = OtherServerIpDetailUiState(inputIp = ""),
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `接続確認中`() =
        composeScreenshotTest {
            mainClock.autoAdvance = false
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
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
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `接続警告あり`() =
        composeScreenshotTest {
            mainClock.autoAdvance = false
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
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
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `検出中表示`() =
        composeScreenshotTest {
            mainClock.autoAdvance = false
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
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
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `見つかったWindows版KoDriverを選択ボタン表示`() =
        composeScreenshotTest {
            mainClock.autoAdvance = false
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                            OtherServerIpDetailPaneContent(
                                uiState =
                                    OtherServerIpDetailUiState(
                                        inputIp = "192.168.1.100",
                                        discoveredServers =
                                            listOf(
                                                DiscoveredServer(
                                                    hostName = "DESKTOP-ABC123",
                                                    ipAddress = "192.168.1.10",
                                                ),
                                            ),
                                        isDiscoveryDialogVisible = false,
                                    ),
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }
}
