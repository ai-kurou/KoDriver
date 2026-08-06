package kurou.kodriver.presentation

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import org.junit.Test

class ConnectionSnackbarEffectTest {
    @Test
    fun `初回チェック結果はスナックバーを表示しない`() =
        composeScreenshotTest {
            var isConnectionChecked by mutableStateOf(false)

            setContent {
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

            isConnectionChecked = true
            waitForIdle()

            onAllNodesWithText(DISCONNECTED_MESSAGE).assertCountEquals(0)
        }

    @Test
    fun `接続中に切断されると接続断メッセージを表示する`() =
        composeScreenshotTest {
            var isConnected by mutableStateOf(false)
            var isConnectionChecked by mutableStateOf(false)

            setContent {
                val snackbarHostState = remember { SnackbarHostState() }
                ConnectionSnackbarEffect(
                    isConnectionChecked = isConnectionChecked,
                    isConnected = isConnected,
                    snackbarHostState = snackbarHostState,
                    connectedMessage = CONNECTED_MESSAGE,
                    disconnectedMessage = DISCONNECTED_MESSAGE,
                )
                SnackbarHost(hostState = snackbarHostState)
            }

            isConnectionChecked = true
            waitForIdle()
            isConnected = true
            waitForIdle()

            onAllNodesWithText(DISCONNECTED_MESSAGE).assertCountEquals(0)

            isConnected = false
            waitForIdle()

            onNodeWithText(DISCONNECTED_MESSAGE).assertIsDisplayed()
        }

    @Test
    fun `切断後に接続されると接続メッセージを表示する`() =
        composeScreenshotTest {
            var isConnected by mutableStateOf(false)

            setContent {
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

            waitForIdle()
            onAllNodesWithText(CONNECTED_MESSAGE).assertCountEquals(0)

            isConnected = true
            waitForIdle()

            onNodeWithText(CONNECTED_MESSAGE).assertIsDisplayed()
        }

    private companion object {
        const val CONNECTED_MESSAGE = "LMUに接続しました"
        const val DISCONNECTED_MESSAGE = "LMUとの接続を待機しています"
    }
}
