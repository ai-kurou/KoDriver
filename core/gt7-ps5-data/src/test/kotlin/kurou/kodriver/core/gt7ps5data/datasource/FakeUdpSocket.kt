package kurou.kodriver.core.gt7ps5data.datasource

import java.net.DatagramPacket
import java.net.SocketTimeoutException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

internal class FakeUdpSocket : UdpSocket {

    // IO スレッドとテストスレッドから同時にアクセスされるため、スレッドセーフなコレクションを使用する
    private val responses: LinkedBlockingDeque<FakeResponse> = LinkedBlockingDeque()
    val sentPackets: CopyOnWriteArrayList<SentPacket> = CopyOnWriteArrayList()

    @Volatile
    var closed = false

    fun enqueuePacket(data: ByteArray) {
        responses.addLast(FakeResponse.Packet(data))
    }

    fun enqueueTimeout() {
        responses.addLast(FakeResponse.Timeout)
    }

    override fun receive(packet: DatagramPacket) {
        // 1ms 待機することで、キャンセル到着前のタイトループを防ぐ
        val response = responses.poll(1L, TimeUnit.MILLISECONDS)
            ?: throw SocketTimeoutException("FakeUdpSocket: no more responses")
        when (response) {
            is FakeResponse.Packet -> {
                response.data.copyInto(packet.data, 0, 0, response.data.size)
                packet.length = response.data.size
            }
            FakeResponse.Timeout -> throw SocketTimeoutException("FakeUdpSocket: simulated timeout")
        }
    }

    override fun send(data: ByteArray, address: String, port: Int) {
        sentPackets.add(SentPacket(data.copyOf(), address, port))
    }

    override fun close() {
        closed = true
        // 待機中の receive() を解放するためにダミーエントリを挿入する
        responses.clear()
    }

    sealed interface FakeResponse {
        data class Packet(val data: ByteArray) : FakeResponse
        data object Timeout : FakeResponse
    }

    data class SentPacket(val data: ByteArray, val address: String, val port: Int)
}
