package kurou.kodriver.feature.acewindowsconnection

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AceWindowsConnectionUiStateTest {
    @Test
    fun `各接続状態の接続判定と確認済み判定`() {
        val connected = AceWindowsConnectionUiState(connectionStatus = AceWindowsConnectionStatus.CONNECTED)
        assertTrue(connected.isConnected)
        assertTrue(connected.isConnectionChecked)

        val unchecked = AceWindowsConnectionUiState(connectionStatus = AceWindowsConnectionStatus.UNCHECKED)
        assertFalse(unchecked.isConnected, "status=${unchecked.connectionStatus} のとき isConnected は false であるべき")
        assertFalse(unchecked.isConnectionChecked)

        val disconnected = AceWindowsConnectionUiState(connectionStatus = AceWindowsConnectionStatus.DISCONNECTED)
        assertFalse(disconnected.isConnected, "status=${disconnected.connectionStatus} のとき isConnected は false であるべき")
        assertTrue(disconnected.isConnectionChecked)
    }
}
