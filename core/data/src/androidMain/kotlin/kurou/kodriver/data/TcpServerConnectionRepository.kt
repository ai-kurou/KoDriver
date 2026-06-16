package kurou.kodriver.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kurou.kodriver.domain.repository.ServerConnectionRepository
import java.net.InetSocketAddress
import java.net.Socket

private const val DEFAULT_PORT = 8080
private const val DEFAULT_TIMEOUT_MS = 3000

internal class TcpServerConnectionRepository(
    private val port: Int = DEFAULT_PORT,
    private val timeoutMs: Int = DEFAULT_TIMEOUT_MS,
) : ServerConnectionRepository {
    // 疎通確認のみで通信データを持たないため非暗号化ソケットで問題ない
    @SuppressWarnings("UNENCRYPTED_SOCKET", "SSRF")
    override suspend fun isConnected(ip: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }
}
