@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherserveripdetail

import kotlinx.coroutines.test.runTest
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TcpServerConnectivityCheckerTest {

    @Test
    fun `サーバーが起動しているポートに接続するとtrueを返す`() = runTest {
        ServerSocket(0).use { server ->
            val checker = TcpServerConnectivityChecker(port = server.localPort)
            assertTrue(checker.isReachable("127.0.0.1"))
        }
    }

    @Test
    fun `サーバーが起動していないポートに接続するとfalseを返す`() = runTest {
        val port = ServerSocket(0).use { it.localPort }
        val checker = TcpServerConnectivityChecker(port = port)
        assertFalse(checker.isReachable("127.0.0.1"))
    }

    @Test
    fun `createServerConnectivityCheckerはTcpServerConnectivityCheckerを返す`() {
        val checker = createServerConnectivityChecker()
        assertIs<TcpServerConnectivityChecker>(checker)
    }
}
