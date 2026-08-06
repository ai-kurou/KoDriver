@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test

class OtherServerIpDiscoveryDialogScreenshotTest {
    private val servers =
        listOf(
            DiscoveredServer(hostName = "DESKTOP-ABC123", ipAddress = "192.168.1.10"),
            DiscoveredServer(hostName = "DESKTOP-XYZ999", ipAddress = "192.168.1.20"),
        )

    @Test
    fun `1台検出時`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                            OtherServerIpDiscoveryDialog(
                                discoveredServers = servers.take(1),
                                selectedDiscoveredServer = servers.first(),
                                onServerSelected = {},
                                onConfirm = {},
                                onDismiss = {},
                            )
                        }
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }

    @Test
    fun `複数台検出時`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(480.dp, 320.dp)) {
                            OtherServerIpDiscoveryDialog(
                                discoveredServers = servers,
                                selectedDiscoveredServer = servers.first(),
                                onServerSelected = {},
                                onConfirm = {},
                                onDismiss = {},
                            )
                        }
                    }
                }
            }
            onNode(isDialog()).captureRoboImage()
        }
}
