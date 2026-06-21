package kurou.kodriver.core.gt7ps5data.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.yield
import java.net.DatagramPacket
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class Gt7Ps5UdpSource(
    private val ps5Address: String,
    private val sendPort: Int = SEND_PORT,
    private val socketFactory: () -> UdpSocket = {
        RealUdpSocket(listenPort = LISTEN_PORT, timeoutMs = SOCKET_TIMEOUT_MS)
    },
    scope: CoroutineScope,
) : Gt7Ps5PacketSource {
    override val packetFlow: Flow<ByteBuffer> = flow {
        socketFactory().use { socket ->
            socket.send(HEARTBEAT_PAYLOAD, ps5Address, sendPort)
            var heartbeatCounter = 0

            val buf = ByteArray(PACKET_SIZE)
            val dgram = DatagramPacket(buf, buf.size)

            while (true) {
                try {
                    socket.receive(dgram)
                    val decrypted = decrypt(buf.copyOf(dgram.length)) ?: continue
                    val bb = ByteBuffer.wrap(decrypted).order(ByteOrder.LITTLE_ENDIAN)
                    if (bb.getInt(MAGIC_OFFSET) != MAGIC) continue
                    emit(bb)

                    heartbeatCounter++
                    if (heartbeatCounter >= HEARTBEAT_INTERVAL_PACKETS) {
                        socket.send(HEARTBEAT_PAYLOAD, ps5Address, sendPort)
                        heartbeatCounter = 0
                    }
                } catch (_: java.net.SocketTimeoutException) {
                    yield()
                    socket.send(HEARTBEAT_PAYLOAD, ps5Address, sendPort)
                }
            }
        }
    }
        .flowOn(Dispatchers.IO)
        .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 0)

    private fun decrypt(data: ByteArray): ByteArray? {
        if (data.size < PACKET_SIZE) return null
        val iv = data.copyOfRange(IV_OFFSET, IV_OFFSET + 8)
        return Salsa20.decrypt(GT7_KEY, iv, data)
    }

    companion object {
        const val LISTEN_PORT = 33740
        const val SEND_PORT = 33739
        const val PACKET_SIZE = 0x128 // 296 bytes
        const val MAGIC_OFFSET = 0
        const val MAGIC = 0x47375330 // 'G750' in little-endian
        const val IV_OFFSET = 0x40
        const val HEARTBEAT_INTERVAL_PACKETS = 100
        const val SOCKET_TIMEOUT_MS = 2_000

        val GT7_KEY = "Btta7y3Gp4kH3p3kLmfqAUVsF0YVsPFr".toByteArray(Charsets.US_ASCII)
        val HEARTBEAT_PAYLOAD = byteArrayOf(0x41, 0x10, 0x00, 0x00)
    }
}
