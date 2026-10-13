package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherServerIpDetailDiscoveryContentTest {
    @get:Rule
    val rule = createComposeRule()

    private val haptic: HapticFeedback = mockk()

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

    @Test
    fun `サーバー行がラジオボタンのroleと選択状態を公開しクリックで一度だけ選択と触感を更新する`() {
        val first = DiscoveredServer(hostName = "DESKTOP-ABC", ipAddress = "192.168.1.50")
        val second = DiscoveredServer(hostName = "DESKTOP-XYZ", ipAddress = "192.168.1.60")
        var selected by mutableStateOf(first)
        val selections = mutableListOf<DiscoveredServer>()
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    OtherServerIpDiscoveryDialog(
                        discoveredServers = listOf(first, second),
                        selectedDiscoveredServer = selected,
                        onServerSelected = {
                            selected = it
                            selections.add(it)
                        },
                        onConfirm = {},
                        onDismiss = {},
                    )
                }
            }
        }

        val radioRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)
        rule
            .onNodeWithText("DESKTOP-ABC (192.168.1.50)")
            .assert(radioRole)
            .assertHeightIsEqualTo(48.dp)
            .assertIsSelected()
        rule
            .onNodeWithText("DESKTOP-XYZ (192.168.1.60)")
            .assert(radioRole)
            .assertHeightIsEqualTo(48.dp)
            .assertIsNotSelected()
            .performClick()
        rule.onNodeWithText("DESKTOP-ABC (192.168.1.50)").assertIsNotSelected()
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        rule.onNodeWithText("DESKTOP-XYZ (192.168.1.60)").assertIsSelected().performClick()

        assertEquals(listOf(second, second), selections)
        verify(exactly = 2) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }
}
