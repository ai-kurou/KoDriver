@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.serverconnection

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ServerConnectionUiStateTest {

    @Test
    fun `isConnected_CONNECTEDのときtrue`() {
        val state = ServerConnectionUiState(connectionStatus = ServerConnectionStatus.CONNECTED)
        assertTrue(state.isConnected)
    }

    @Test
    fun `isConnected_CONNECTED以外のときfalse`() {
        for (status in listOf(
            ServerConnectionStatus.NOT_CONFIGURED,
            ServerConnectionStatus.CHECKING,
            ServerConnectionStatus.DISCONNECTED,
        )) {
            val state = ServerConnectionUiState(connectionStatus = status)
            assertFalse(state.isConnected, "status=$status のとき isConnected は false であるべき")
        }
    }

    @Test
    fun `isConnectionChecked_CONNECTEDのときtrue`() {
        val state = ServerConnectionUiState(connectionStatus = ServerConnectionStatus.CONNECTED)
        assertTrue(state.isConnectionChecked)
    }

    @Test
    fun `isConnectionChecked_DISCONNECTEDのときtrue`() {
        val state = ServerConnectionUiState(connectionStatus = ServerConnectionStatus.DISCONNECTED)
        assertTrue(state.isConnectionChecked)
    }

    @Test
    fun `isConnectionChecked_NOT_CONFIGUREDのときfalse`() {
        val state = ServerConnectionUiState(connectionStatus = ServerConnectionStatus.NOT_CONFIGURED)
        assertFalse(state.isConnectionChecked)
    }

    @Test
    fun `isConnectionChecked_CHECKINGのときfalse`() {
        val state = ServerConnectionUiState(connectionStatus = ServerConnectionStatus.CHECKING)
        assertFalse(state.isConnectionChecked)
    }

    @Test
    fun `isIpConfigured_NOT_CONFIGURED以外のときtrue`() {
        for (status in listOf(
            ServerConnectionStatus.CHECKING,
            ServerConnectionStatus.CONNECTED,
            ServerConnectionStatus.DISCONNECTED,
        )) {
            val state = ServerConnectionUiState(connectionStatus = status)
            assertTrue(state.isIpConfigured, "status=$status のとき isIpConfigured は true であるべき")
        }
    }

    @Test
    fun `isIpConfigured_NOT_CONFIGUREDのときfalse`() {
        val state = ServerConnectionUiState(connectionStatus = ServerConnectionStatus.NOT_CONFIGURED)
        assertFalse(state.isIpConfigured)
    }
}
