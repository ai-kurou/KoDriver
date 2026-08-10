package kurou.kodriver.feature.otherserveripdetail

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

private const val DEFAULT_PORT = 8080
private const val TIMEOUT_MS = 3000

internal class TcpServerConnectivityChecker(
    private val port: Int = DEFAULT_PORT,
) : ServerConnectivityChecker {
    override suspend fun isReachable(ip: String): Boolean =
        withContext(Dispatchers.IO) {
            val address = ip.toIpv4InetAddress() ?: return@withContext false
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(address, port), TIMEOUT_MS)
                    true
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                false
            }
        }
}

internal actual fun createServerConnectivityChecker(): ServerConnectivityChecker = TcpServerConnectivityChecker()

private fun String.toIpv4InetAddress(): InetAddress? {
    val octets = split(".")
    if (octets.size != 4) return null

    val address = ByteArray(4)
    octets.forEachIndexed { index, octet ->
        val value = octet.toIntOrNull() ?: return null
        if (value !in 0..255) return null
        address[index] = value.toByte()
    }
    return InetAddress.getByAddress(address)
}
