@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TcpServerConnectionRepositoryTest {

    private lateinit var serverSocket: ServerSocket

    @Before
    @SuppressWarnings("UNENCRYPTED_SERVER_SOCKET")
    fun setUp() {
        serverSocket = ServerSocket(0)
    }

    @After
    fun tearDown() {
        serverSocket.close()
    }

    private fun buildRepository() = TcpServerConnectionRepository(
        port = serverSocket.localPort,
        timeoutMs = 1000,
    )

    @Test
    fun `サーバーが起動しているときtrueを返す`() = runTest {
        assertTrue(buildRepository().isConnected("127.0.0.1"))
    }

    @Test
    fun `サーバーが停止しているときfalseを返す`() = runTest {
        serverSocket.close()
        assertFalse(buildRepository().isConnected("127.0.0.1"))
    }

    @Test
    fun `到達不能なIPアドレスのときfalseを返す`() = runTest {
        val repository = TcpServerConnectionRepository(port = serverSocket.localPort, timeoutMs = 100)
        assertFalse(repository.isConnected("192.0.2.1"))
    }
}
