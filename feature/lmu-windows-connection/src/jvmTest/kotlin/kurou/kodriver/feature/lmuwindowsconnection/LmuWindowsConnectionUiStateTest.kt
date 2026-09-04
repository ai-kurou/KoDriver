@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsconnection

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LmuWindowsConnectionUiStateTest {
    @Test
    fun `isConnected_CONNECTEDのときtrue`() {
        val state = LmuWindowsConnectionUiState(connectionStatus = LmuWindowsConnectionStatus.CONNECTED)
        assertTrue(state.isConnected)
    }

    @Test
    fun `isConnected_CONNECTED以外のときfalse`() {
        for (status in listOf(
            LmuWindowsConnectionStatus.UNCHECKED,
            LmuWindowsConnectionStatus.DISCONNECTED,
        )) {
            val state = LmuWindowsConnectionUiState(connectionStatus = status)
            assertFalse(state.isConnected, "status=$status のとき isConnected は false であるべき")
        }
    }

    @Test
    fun `isConnectionChecked_CONNECTEDのときtrue`() {
        val state = LmuWindowsConnectionUiState(connectionStatus = LmuWindowsConnectionStatus.CONNECTED)
        assertTrue(state.isConnectionChecked)
    }

    @Test
    fun `isConnectionChecked_DISCONNECTEDのときtrue`() {
        val state = LmuWindowsConnectionUiState(connectionStatus = LmuWindowsConnectionStatus.DISCONNECTED)
        assertTrue(state.isConnectionChecked)
    }

    @Test
    fun `isConnectionChecked_UNCHECKEDのときfalse`() {
        val state = LmuWindowsConnectionUiState(connectionStatus = LmuWindowsConnectionStatus.UNCHECKED)
        assertFalse(state.isConnectionChecked)
    }
}
