package kurou.kodriver.core.gt7ps5data.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
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
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_SOCKET_TIMEOUT_MILLIS
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_UDP_PORT_DEFAULT
import java.net.DatagramPacket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Clock

private fun ByteArray.readIntLE(offset: Int): Int =
    (this[offset].toInt() and 0xFF) or
        ((this[offset + 1].toInt() and 0xFF) shl 8) or
        ((this[offset + 2].toInt() and 0xFF) shl 16) or
        ((this[offset + 3].toInt() and 0xFF) shl 24)

private fun ByteArray.writeIntLE(
    offset: Int,
    value: Int,
) {
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
        RealUdpSocket(listenPort = port, timeoutMs = GT7_PS5_SOCKET_TIMEOUT_MILLIS)
    },
    scope: CoroutineScope,
    private val currentTimeMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : Gt7Ps5PacketSource {
    private val _lastPacketReceivedAt = AtomicLong(0L)

    override fun lastPacketReceivedAt(): Long = _lastPacketReceivedAt.get()

    override val packetFlow: Flow<ByteBuffer> =
        combine(consoleAddressFlow, listenPortFlow) { address, port ->
            address to port
        }.flatMapLatest { (address, port) ->
            if (address.isNullOrBlank()) {
                emptyFlow()
            } else {
                udpPacketFlow(address, port)
            }
        }.shareIn(scope, SharingStarted.WhileSubscribed(), replay = 0)

    private fun udpPacketFlow(
        ps5Address: String,
        listenPort: Int,
    ): Flow<ByteBuffer> =
        flow {
            socketFactory(listenPort).use { socket -> receiveLoop(socket, ps5Address) }
        }.retryWhen { cause, attempt ->
            // Wi-Fi切り替え・スリープ復帰時のsend失敗、ポート競合(BindException)など、
            // 一時的なネットワーク起因のIOExceptionは再接続を試みる
            if (cause is java.io.IOException) {
                delay(bindRetryDelayMillis(attempt))
                true
            } else {
                false
            }
        }.flowOn(Dispatchers.IO)

    private suspend fun FlowCollector<ByteBuffer>.receiveLoop(
        socket: UdpSocket,
        ps5Address: String,
    ) {
        // 最後にハートビートを送った時刻。不正パケットだけが続く間の時間経過再送の判定に使う
        var lastHeartbeatAt = 0L

        fun sendHeartbeat() {
            socket.send(HEARTBEAT_PAYLOAD, ps5Address, sendPort)
            lastHeartbeatAt = currentTimeMillis()
        }

        sendHeartbeat()
        var heartbeatCounter = 0

        val buf = ByteArray(PACKET_MIN_SIZE)
        val dgram = DatagramPacket(buf, buf.size)

        while (true) {
            try {
                val packet = receivePacket(socket, buf, dgram)
                if (packet == null) {
                    // 不正パケットは emit しないためキャンセルを検知できない。ここで確認しないと購読解除・ポート変更が効かない
                    currentCoroutineContext().ensureActive()
                    if (currentTimeMillis() - lastHeartbeatAt >= HEARTBEAT_INTERVAL_MILLIS) sendHeartbeat()
                    continue
                }
                _lastPacketReceivedAt.set(currentTimeMillis())
                emit(packet)

                heartbeatCounter++
                if (heartbeatCounter >= HEARTBEAT_INTERVAL_PACKETS) {
                    sendHeartbeat()
                    heartbeatCounter = 0
                }
            } catch (_: java.net.SocketTimeoutException) {
                yield()
                sendHeartbeat()
            }
        }
    }

    /** 復号して GT7 のパケットと確認できた場合のみ返し、短いパケットやマジック不一致は null を返す。 */
    private fun receivePacket(
        socket: UdpSocket,
        buf: ByteArray,
        dgram: DatagramPacket,
    ): ByteBuffer? {
        // receive は length を受信サイズへ書き換えるため、短いパケットの後も全長を受け取れるよう戻す。
        dgram.length = buf.size
        socket.receive(dgram)
        val decrypted = decrypt(buf.copyOf(dgram.length)) ?: return null
        val bb = ByteBuffer.wrap(decrypted).order(ByteOrder.LITTLE_ENDIAN)
        return if (bb.getInt(MAGIC_OFFSET) == MAGIC) bb else null
    }

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
        const val LISTEN_PORT = GT7_PS5_UDP_PORT_DEFAULT
        const val SEND_PORT = 33739
        const val PACKET_MIN_SIZE = 0x170 // 368 bytes
        const val MAGIC_OFFSET = 0
        const val MAGIC = 0x47375330 // 'G7S0' in little-endian
        const val IV_OFFSET = 0x40
        const val HEARTBEAT_INTERVAL_PACKETS = 100

        /** 不正パケットだけが続く間に、前回送信からこの時間経過したらハートビートを再送する。 */
        const val HEARTBEAT_INTERVAL_MILLIS = GT7_PS5_SOCKET_TIMEOUT_MILLIS.toLong()
        const val BIND_RETRY_DELAY_MS = 1_000L

        /** 再接続待ちの上限。attempt は再接続に成功しても戻らないため、上限がないと待ちが延び続ける。 */
        const val BIND_RETRY_MAX_DELAY_MS = 5_000L

        internal fun bindRetryDelayMillis(attempt: Long): Long =
            minOf(BIND_RETRY_DELAY_MS * (attempt + 1), BIND_RETRY_MAX_DELAY_MS)

        val GT7_KEY = "Simulator Interface Packet GT7 ver 0.0".toByteArray().copyOf(32)
        val HEARTBEAT_PAYLOAD = "C".toByteArray(Charsets.UTF_8)
    }
}
