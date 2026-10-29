package kurou.kodriver.feature.gt7ps5connection

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Gt7Ps5ConnectionUiStateTest {
    @Test
    fun `各接続状態の接続判定と確認済み判定`() {
        val connected = Gt7Ps5ConnectionUiState(connectionStatus = Gt7Ps5ConnectionStatus.CONNECTED)
        assertTrue(connected.isConnected)
        assertTrue(connected.isConnectionChecked)

        val unchecked = Gt7Ps5ConnectionUiState(connectionStatus = Gt7Ps5ConnectionStatus.UNCHECKED)
        assertFalse(unchecked.isConnected, "status=${unchecked.connectionStatus} のとき isConnected は false であるべき")
        assertFalse(unchecked.isConnectionChecked)

        val disconnected = Gt7Ps5ConnectionUiState(connectionStatus = Gt7Ps5ConnectionStatus.DISCONNECTED)
        assertFalse(disconnected.isConnected, "status=${disconnected.connectionStatus} のとき isConnected は false であるべき")
        assertTrue(disconnected.isConnectionChecked)
    }
}
