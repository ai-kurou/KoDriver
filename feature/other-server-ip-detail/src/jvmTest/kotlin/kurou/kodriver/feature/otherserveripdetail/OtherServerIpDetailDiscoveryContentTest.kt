@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherServerIpDetailDiscoveryContentTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `検出中かつ検出結果がない場合は検出中の表示がされる`() {
        rule.setContent {
            OtherServerIpDetailPaneContent(
                uiState =
                    OtherServerIpDetailUiState(
                    inputIp = "192.168.1.1",
                    discoveredServers = emptyList(),
                ),
                    )
        }

        rule.onNodeWithText("Windows版KoDriverを検出中…").assertIsDisplayed()
    }

    @Test
    fun `検出中は検出ボタンが無効になっている`() {
        rule.setContent {
            OtherServerIpDetailPaneContent(
                uiState =
                    OtherServerIpDetailUiState(
                    inputIp = "192.168.1.1",
                    discoveredServers = emptyList(),
                ),
                    )
        }

        rule.onNodeWithText("Windows版KoDriverを検出中…").assertIsNotEnabled()
    }

    @Test
    fun `検出したサーバーがある場合検出ダイアログが表示される`() {
        val server = DiscoveredServer(hostName = "DESKTOP-ABC", ipAddress = "192.168.1.50")
        rule.setContent {
            OtherServerIpDetailPaneContent(
                uiState =
                    OtherServerIpDetailUiState(
                    inputIp = "192.168.1.1",
                    discoveredServers = listOf(server),
                    isDiscoveryDialogVisible = true,
                    selectedDiscoveredServer = server,
                ),
                    )
        }

        rule.onNodeWithText("Windows版KoDriverを選択").assertIsDisplayed()
        rule.onNodeWithText("DESKTOP-ABC (192.168.1.50)").assertIsDisplayed()
    }

    @Test
    fun `検出ダイアログで選択ボタンをクリックするとonDiscoveryDialogConfirmが呼ばれる`() {
        val server = DiscoveredServer(hostName = "DESKTOP-ABC", ipAddress = "192.168.1.50")
        var confirmCount = 0
        rule.setContent {
            OtherServerIpDetailPaneContent(
                uiState =
                    OtherServerIpDetailUiState(
                    inputIp = "192.168.1.1",
                    discoveredServers = listOf(server),
                    isDiscoveryDialogVisible = true,
                    selectedDiscoveredServer = server,
                ),
                    onDiscoveryDialogConfirm = { confirmCount++ },
            )
        }

        rule.onNodeWithText("OK").performClick()

        assertEquals(1, confirmCount)
    }

    @Test
    fun `検出ダイアログでキャンセルボタンをクリックするとonDiscoveryDialogDismissが呼ばれる`() {
        val server = DiscoveredServer(hostName = "DESKTOP-ABC", ipAddress = "192.168.1.50")
        var dismissCount = 0
        rule.setContent {
            OtherServerIpDetailPaneContent(
                uiState =
                    OtherServerIpDetailUiState(
                    inputIp = "192.168.1.1",
                    discoveredServers = listOf(server),
                    isDiscoveryDialogVisible = true,
                    selectedDiscoveredServer = server,
                ),
                    onDiscoveryDialogDismiss = { dismissCount++ },
            )
        }

        rule.onNodeWithText("キャンセル").performClick()

        assertEquals(1, dismissCount)
    }

    @Test
    fun `見つかったWindows版KoDriverを選択ボタンをクリックするとonShowDiscoveredServersが呼ばれる`() {
        val server = DiscoveredServer(hostName = "DESKTOP-ABC", ipAddress = "192.168.1.50")
        var showCount = 0
        rule.setContent {
            OtherServerIpDetailPaneContent(
                uiState =
                    OtherServerIpDetailUiState(
                    inputIp = "192.168.1.1",
                    discoveredServers = listOf(server),
                    isDiscoveryDialogVisible = false,
                ),
                    onShowDiscoveredServers = { showCount++ },
            )
        }

        rule.onNodeWithText("見つかったWindows版KoDriverを選択").performClick()

        assertEquals(1, showCount)
    }
}
