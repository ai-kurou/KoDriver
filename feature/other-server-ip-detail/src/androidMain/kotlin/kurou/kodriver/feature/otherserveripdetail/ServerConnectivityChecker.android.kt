package kurou.kodriver.feature.otherserveripdetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

private const val DEFAULT_PORT = 8080
private const val TIMEOUT_MS = 3000

internal class TcpServerConnectivityChecker(
    private val port: Int = DEFAULT_PORT,
) : ServerConnectivityChecker {
    override suspend fun isReachable(ip: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), TIMEOUT_MS)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}

internal actual fun createServerConnectivityChecker(): ServerConnectivityChecker =
    TcpServerConnectivityChecker()
