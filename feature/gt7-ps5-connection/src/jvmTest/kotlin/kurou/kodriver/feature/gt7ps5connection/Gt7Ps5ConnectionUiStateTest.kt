@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5connection

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Gt7Ps5ConnectionUiStateTest {
    @Test
    fun `isConnected_CONNECTEDのときtrue`() {
        val state = Gt7Ps5ConnectionUiState(connectionStatus = Gt7Ps5ConnectionStatus.CONNECTED)
        assertTrue(state.isConnected)
    }

    @Test
    fun `isConnected_CONNECTED以外のときfalse`() {
        for (status in listOf(
            Gt7Ps5ConnectionStatus.UNCHECKED,
            Gt7Ps5ConnectionStatus.DISCONNECTED,
        )) {
            val state = Gt7Ps5ConnectionUiState(connectionStatus = status)
            assertFalse(state.isConnected, "status=$status のとき isConnected は false であるべき")
        }
    }

    @Test
    fun `isConnectionChecked_CONNECTEDのときtrue`() {
        val state = Gt7Ps5ConnectionUiState(connectionStatus = Gt7Ps5ConnectionStatus.CONNECTED)
        assertTrue(state.isConnectionChecked)
    }

    @Test
    fun `isConnectionChecked_DISCONNECTEDのときtrue`() {
        val state = Gt7Ps5ConnectionUiState(connectionStatus = Gt7Ps5ConnectionStatus.DISCONNECTED)
        assertTrue(state.isConnectionChecked)
    }

    @Test
    fun `isConnectionChecked_UNCHECKEDのときfalse`() {
        val state = Gt7Ps5ConnectionUiState(connectionStatus = Gt7Ps5ConnectionStatus.UNCHECKED)
        assertFalse(state.isConnectionChecked)
    }
}
