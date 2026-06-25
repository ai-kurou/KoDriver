package kurou.kodriver.core.gt7ps5data.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.yield
import java.net.DatagramPacket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicLong

private fun ByteArray.readIntLE(offset: Int): Int =
    (this[offset].toInt() and 0xFF) or
        ((this[offset + 1].toInt() and 0xFF) shl 8) or
        ((this[offset + 2].toInt() and 0xFF) shl 16) or
        ((this[offset + 3].toInt() and 0xFF) shl 24)

private fun ByteArray.writeIntLE(offset: Int, value: Int) {
    this[offset] = (value and 0xFF).toByte()
    this[offset + 1] = ((value ushr 8) and 0xFF).toByte()
    this[offset + 2] = ((value ushr 16) and 0xFF).toByte()
    this[offset + 3] = ((value ushr 24) and 0xFF).toByte()
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class Gt7Ps5UdpSource(
    private val consoleAddressFlow: Flow<String?>,
    private val listenPortFlow: Flow<Int> = flowOf(LISTEN_PORT),
    private val sendPort: Int = SEND_PORT,
    private val socketFactory: (Int) -> UdpSocket = { port ->
        RealUdpSocket(listenPort = port, timeoutMs = SOCKET_TIMEOUT_MS)
    },
    scope: CoroutineScope,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : Gt7Ps5PacketSource {

    private val _lastPacketReceivedAt = AtomicLong(0L)

    override fun lastPacketReceivedAt(): Long = _lastPacketReceivedAt.get()

    override val packetFlow: Flow<ByteBuffer> = combine(consoleAddressFlow, listenPortFlow) { address, port ->
        address to port
    }
        .flatMapLatest { (address, port) ->
            if (address.isNullOrBlank()) {
                emptyFlow()
            } else {
                udpPacketFlow(address, port)
            }
        }
        .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 0)

    private fun udpPacketFlow(ps5Address: String, listenPort: Int): Flow<ByteBuffer> = flow {
        socketFactory(listenPort).use { socket ->
            socket.send(HEARTBEAT_PAYLOAD, ps5Address, sendPort)
            var heartbeatCounter = 0

            val buf = ByteArray(PACKET_MIN_SIZE)
            val dgram = DatagramPacket(buf, buf.size)

            while (true) {
                try {
                    socket.receive(dgram)
                    val decrypted = decrypt(buf.copyOf(dgram.length)) ?: continue
                    val bb = ByteBuffer.wrap(decrypted).order(ByteOrder.LITTLE_ENDIAN)
                    if (bb.getInt(MAGIC_OFFSET) != MAGIC) continue
                    _lastPacketReceivedAt.set(currentTimeMillis())
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
    }.retryWhen { cause, attempt ->
        if (cause is java.net.BindException) {
            delay(BIND_RETRY_DELAY_MS * (attempt + 1))
            true
        } else {
            false
        }
    }.flowOn(Dispatchers.IO)

    private fun decrypt(data: ByteArray): ByteArray? {
        if (data.size < PACKET_MIN_SIZE) return null
        val iv1 = data.readIntLE(IV_OFFSET)
        val iv2 = iv1 xor 0xDEADBEEF.toInt()
        val nonce = ByteArray(8)
        nonce.writeIntLE(0, iv2)
        nonce.writeIntLE(4, iv1)
        return Salsa20.decrypt(GT7_KEY, nonce, data)
    }

    companion object {
        const val LISTEN_PORT = 33740
        const val SEND_PORT = 33739
        const val PACKET_MIN_SIZE = 0x170 // 368 bytes
        const val MAGIC_OFFSET = 0
        const val MAGIC = 0x47375330 // 'G7S0' in little-endian
        const val IV_OFFSET = 0x40
        const val HEARTBEAT_INTERVAL_PACKETS = 100
        const val SOCKET_TIMEOUT_MS = 3_000
        const val BIND_RETRY_DELAY_MS = 1_000L

        val GT7_KEY = "Simulator Interface Packet GT7 ver 0.0".toByteArray().copyOf(32)
        val HEARTBEAT_PAYLOAD = "C".toByteArray(Charsets.UTF_8)
    }
}
