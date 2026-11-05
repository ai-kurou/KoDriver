package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherServerIpDiscoveryDialogTest {
    @get:Rule
    val rule = createComposeRule()

    private val haptic: HapticFeedback = mockk()

    private val servers =
        listOf(
            DiscoveredServer(hostName = "DESKTOP-ABC123", ipAddress = "192.168.1.10"),
            DiscoveredServer(hostName = "DESKTOP-XYZ999", ipAddress = "192.168.1.20"),
        )

    private fun setContent(
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    OtherServerIpDiscoveryDialog(
                        discoveredServers = servers,
                        selectedDiscoveredServer = servers.first(),
                        onServerSelected = {},
                        onConfirm = onConfirm,
                        onDismiss = onDismiss,
                    )
                }
            }
        }
    }

    @Test
    fun `OKボタンをクリックするとハプティックとonConfirmが一度ずつ呼ばれる`() {
        var confirmCount = 0
        var dismissCount = 0
        setContent(
            onConfirm = { confirmCount++ },
            onDismiss = { dismissCount++ },
        )

        rule.onNodeWithText("OK").performClick()

        assertEquals(1, confirmCount)
        assertEquals(0, dismissCount)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `キャンセルボタンをクリックするとハプティックとonDismissが一度ずつ呼ばれる`() {
        var confirmCount = 0
        var dismissCount = 0
        setContent(
            onConfirm = { confirmCount++ },
            onDismiss = { dismissCount++ },
        )

        rule.onNodeWithText("キャンセル").performClick()

        assertEquals(0, confirmCount)
        assertEquals(1, dismissCount)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }
}
