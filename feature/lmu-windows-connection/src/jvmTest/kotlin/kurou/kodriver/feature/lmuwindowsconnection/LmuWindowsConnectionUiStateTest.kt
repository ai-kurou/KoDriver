package kurou.kodriver.feature.lmuwindowsconnection

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LmuWindowsConnectionUiStateTest {
    @Test
    fun `各接続状態の接続判定と確認済み判定`() {
        val connected = LmuWindowsConnectionUiState(connectionStatus = LmuWindowsConnectionStatus.CONNECTED)
        assertTrue(connected.isConnected)
        assertTrue(connected.isConnectionChecked)

        val unchecked = LmuWindowsConnectionUiState(connectionStatus = LmuWindowsConnectionStatus.UNCHECKED)
        assertFalse(unchecked.isConnected, "status=${unchecked.connectionStatus} のとき isConnected は false であるべき")
        assertFalse(unchecked.isConnectionChecked)

        val disconnected = LmuWindowsConnectionUiState(connectionStatus = LmuWindowsConnectionStatus.DISCONNECTED)
        assertFalse(disconnected.isConnected, "status=${disconnected.connectionStatus} のとき isConnected は false であるべき")
        assertTrue(disconnected.isConnectionChecked)
    }
}
