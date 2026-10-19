@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherserveripdetail

import kotlinx.coroutines.runBlocking
import java.net.InetAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TcpServerConnectivityCheckerTest {
    @Test
    fun `IPv4アドレスを4つのオクテットに変換して接続に渡す`() =
        runBlocking {
            var connectedAddress: InetAddress? = null
            var connectedPort: Int? = null
            var connectedTimeoutMs: Int? = null
            val checker =
                TcpServerConnectivityChecker(port = 4321) { address, port, timeoutMs ->
                    connectedAddress = address
                    connectedPort = port
                    connectedTimeoutMs = timeoutMs
                }

            assertTrue(checker.isReachable("0.1.255.254"))
            assertEquals("0.1.255.254", connectedAddress?.hostAddress)
            assertEquals(4321, connectedPort)
            assertEquals(3000, connectedTimeoutMs)
        }

    @Test
    fun `要素数が4ではないIPv4アドレスは接続せずfalseを返す`() =
        runBlocking {
            var connectCalled = false
            val checker = TcpServerConnectivityChecker(connect = { _, _, _ -> connectCalled = true })

            assertFalse(checker.isReachable("127.0.1"))
            assertFalse(checker.isReachable("127.0.0.1.2"))
            assertFalse(connectCalled)
        }

    @Test
    fun `数値に変換できないオクテットは接続せずfalseを返す`() =
        runBlocking {
            var connectCalled = false
            val checker = TcpServerConnectivityChecker(connect = { _, _, _ -> connectCalled = true })

            assertFalse(checker.isReachable("127.x.0.1"))
            assertFalse(checker.isReachable("127..0.1"))
            assertFalse(connectCalled)
        }

    @Test
    fun `範囲外のオクテットは接続せずfalseを返す`() =
        runBlocking {
            var connectCalled = false
            val checker = TcpServerConnectivityChecker(connect = { _, _, _ -> connectCalled = true })

            assertFalse(checker.isReachable("-1.0.0.1"))
            assertFalse(checker.isReachable("256.0.0.1"))
            assertFalse(connectCalled)
        }
}
