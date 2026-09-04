@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.acewindowsconnection

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AceWindowsConnectionUiStateTest {
    @Test
    fun `isConnected_CONNECTEDのときtrue`() {
        val state = AceWindowsConnectionUiState(connectionStatus = AceWindowsConnectionStatus.CONNECTED)
        assertTrue(state.isConnected)
    }

    @Test
    fun `isConnected_CONNECTED以外のときfalse`() {
        for (status in listOf(
            AceWindowsConnectionStatus.UNCHECKED,
            AceWindowsConnectionStatus.DISCONNECTED,
        )) {
            val state = AceWindowsConnectionUiState(connectionStatus = status)
            assertFalse(state.isConnected, "status=$status のとき isConnected は false であるべき")
        }
    }

    @Test
    fun `isConnectionChecked_CONNECTEDのときtrue`() {
        val state = AceWindowsConnectionUiState(connectionStatus = AceWindowsConnectionStatus.CONNECTED)
        assertTrue(state.isConnectionChecked)
    }

    @Test
    fun `isConnectionChecked_DISCONNECTEDのときtrue`() {
        val state = AceWindowsConnectionUiState(connectionStatus = AceWindowsConnectionStatus.DISCONNECTED)
        assertTrue(state.isConnectionChecked)
    }

    @Test
    fun `isConnectionChecked_UNCHECKEDのときfalse`() {
        val state = AceWindowsConnectionUiState(connectionStatus = AceWindowsConnectionStatus.UNCHECKED)
        assertFalse(state.isConnectionChecked)
    }
}
