package kurou.kodriver.core.gt7ps5data.datasource

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketException
import java.net.SocketTimeoutException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RealUdpSocketTest {

    @Test
    fun `receiveで送信されたデータを受信できる`() {
        val socket = RealUdpSocket(listenPort = 0, timeoutMs = 1_000)
        val port = boundPort(socket)

        val sender = DatagramSocket()
        val payload = byteArrayOf(1, 2, 3, 4)
        sender.send(java.net.DatagramPacket(payload, payload.size, java.net.InetAddress.getLoopbackAddress(), port))
        sender.close()

        val buf = ByteArray(64)
        val packet = DatagramPacket(buf, buf.size)
        socket.receive(packet)

        assertContentEquals(payload, buf.copyOf(packet.length))
        socket.close()
    }

    @Test
    fun `sendで指定アドレスとポートへデータを送信できる`() {
        val receiver = DatagramSocket(null).apply {
            reuseAddress = true
            bind(InetSocketAddress(InetAddress.getLoopbackAddress(), 0))
            soTimeout = 1_000
        }
        val receiverPort = receiver.localPort

        val socket = RealUdpSocket(listenPort = 0, timeoutMs = 1_000)
        val payload = byteArrayOf(10, 20, 30)
        socket.send(payload, InetAddress.getLoopbackAddress().hostAddress, receiverPort)

        val buf = ByteArray(64)
        val packet = DatagramPacket(buf, buf.size)
        receiver.receive(packet)

        assertContentEquals(payload, buf.copyOf(packet.length))
        receiver.close()
        socket.close()
    }

    @Test
    fun `タイムアウト時間内にパケットが届かない場合SocketTimeoutExceptionをスローする`() {
        val socket = RealUdpSocket(listenPort = 0, timeoutMs = 100)

        val buf = ByteArray(64)
        val packet = DatagramPacket(buf, buf.size)
        assertFailsWith<SocketTimeoutException> { socket.receive(packet) }

        socket.close()
    }

    @Test
    fun `close後にreceiveを呼ぶとSocketExceptionをスローする`() {
        val socket = RealUdpSocket(listenPort = 0, timeoutMs = 1_000)
        socket.close()

        val buf = ByteArray(64)
        val packet = DatagramPacket(buf, buf.size)
        assertFailsWith<SocketException> { socket.receive(packet) }
    }

    @Test
    fun `同一ポートにreuseAddressで複数回バインドできる`() {
        val first = RealUdpSocket(listenPort = 0, timeoutMs = 100)
        val port = boundPort(first)
        first.close()

        val second = RealUdpSocket(listenPort = port, timeoutMs = 100)
        assertEquals(port, boundPort(second))
        second.close()
    }

    private fun boundPort(socket: RealUdpSocket): Int {
        val field = socket.javaClass.getDeclaredField("socket")
        field.isAccessible = true
        return (field.get(socket) as DatagramSocket).localPort
    }
}
