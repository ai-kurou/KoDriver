package kurou.kodriver.presentation

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ConnectionSnackbarEffectTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `初回確認前は表示せず未接続の確認後に接続断を表示する`() {
        var isConnectionChecked by mutableStateOf(false)

        composeRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            ConnectionSnackbarEffect(
                isConnectionChecked = isConnectionChecked,
                isConnected = false,
                snackbarHostState = snackbarHostState,
                connectedMessage = CONNECTED_MESSAGE,
                disconnectedMessage = DISCONNECTED_MESSAGE,
            )
            SnackbarHost(hostState = snackbarHostState)
        }

        composeRule.onAllNodesWithText(DISCONNECTED_MESSAGE).assertCountEquals(0)

        isConnectionChecked = true
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DISCONNECTED_MESSAGE).assertIsDisplayed()
    }

    @Test
    fun `接続状態が変化するたびに対応するメッセージを表示する`() {
        var isConnected by mutableStateOf(true)

        composeRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            ConnectionSnackbarEffect(
                isConnectionChecked = true,
                isConnected = isConnected,
                snackbarHostState = snackbarHostState,
                connectedMessage = CONNECTED_MESSAGE,
                disconnectedMessage = DISCONNECTED_MESSAGE,
            )
            SnackbarHost(hostState = snackbarHostState)
        }

        composeRule.onNodeWithText(CONNECTED_MESSAGE).assertIsDisplayed()

        isConnected = false
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DISCONNECTED_MESSAGE).assertIsDisplayed()
    }

    private companion object {
        const val CONNECTED_MESSAGE = "LMUに接続しました"
        const val DISCONNECTED_MESSAGE = "LMUとの接続が切れました。再接続を待機しています"
    }
}
