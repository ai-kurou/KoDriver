package kurou.kodriver.core.gt7ps5data.datasource

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

internal interface UdpSocket : AutoCloseable {
    fun receive(packet: DatagramPacket)
    fun send(data: ByteArray, address: String, port: Int)
}

internal class RealUdpSocket(listenPort: Int, timeoutMs: Int) : UdpSocket {
    private val socket = DatagramSocket(null).apply {
        reuseAddress = true
        bind(java.net.InetSocketAddress(listenPort))
        soTimeout = timeoutMs
    }

    override fun receive(packet: DatagramPacket) = socket.receive(packet)

    override fun send(data: ByteArray, address: String, port: Int) {
        val addr = InetAddress.getByName(address)
        socket.send(DatagramPacket(data, data.size, addr, port))
    }

    override fun close() = socket.close()
}
