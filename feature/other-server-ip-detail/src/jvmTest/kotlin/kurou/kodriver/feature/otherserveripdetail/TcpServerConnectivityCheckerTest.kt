@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherserveripdetail

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import kotlinx.coroutines.test.runTest
import java.io.IOException
import java.net.ServerSocket
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TcpServerConnectivityCheckerTest {
    @AfterTest
    fun tearDown() {
        unmockkStatic(Sentry::class)
    }

    @Test
    fun `サーバーが起動しているポートに接続するとtrueを返す`() =
        runTest {
            ServerSocket(0).use { server ->
                val checker = TcpServerConnectivityChecker(port = server.localPort)
                assertTrue(checker.isReachable("127.0.0.1"))
            }
        }

    @Test
    fun `サーバーが起動していないポートに接続するとfalseを返す`() =
        runTest {
            val port = ServerSocket(0).use { it.localPort }
            val checker = TcpServerConnectivityChecker(port = port)
            assertFalse(checker.isReachable("127.0.0.1"))
        }

    @Test
    fun `IPv4アドレスではない接続先はfalseを返す`() =
        runTest {
            ServerSocket(0).use { server ->
                val checker = TcpServerConnectivityChecker(port = server.localPort)
                assertFalse(checker.isReachable("localhost"))
            }
        }

    @Test
    fun `接続時にIOExceptionが発生した場合はSentryに記録せずfalseを返す`() =
        runTest {
            val exception = IOException("接続タイムアウト")
            val checker =
                TcpServerConnectivityChecker(connect = { _, _, _ -> throw exception })

            assertFalse(checker.isReachable("127.0.0.1"))
        }

    @Test
    fun `接続時にIOException以外の例外が発生した場合はSentryに記録してfalseを返す`() =
        runTest {
            val exception = SecurityException("想定外のエラー")
            mockkStatic(Sentry::class)
            every { Sentry.captureException(exception) } returns SentryId.EMPTY_ID
            val checker =
                TcpServerConnectivityChecker(connect = { _, _, _ -> throw exception })

            assertFalse(checker.isReachable("127.0.0.1"))

            verify(exactly = 1) { Sentry.captureException(exception) }
            confirmVerified(Sentry)
        }

    @Test
    fun `createServerConnectivityCheckerはTcpServerConnectivityCheckerを返す`() {
        val checker = createServerConnectivityChecker()
        assertIs<TcpServerConnectivityChecker>(checker)
    }
}
