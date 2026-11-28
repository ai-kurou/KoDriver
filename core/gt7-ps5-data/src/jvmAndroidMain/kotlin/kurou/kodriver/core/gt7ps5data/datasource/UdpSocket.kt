package kurou.kodriver.core.gt7ps5data.datasource

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

internal interface UdpSocket : AutoCloseable {
    fun receive(packet: DatagramPacket)

    fun send(
        data: ByteArray,
        address: String,
        port: Int,
    )
}

internal class RealUdpSocket(
    listenPort: Int,
    timeoutMs: Int,
    createSocket: () -> DatagramSocket = { DatagramSocket(null) },
) : UdpSocket {
    private val socket: DatagramSocket =
        createSocket().also { created ->
            // 設定・bind の途中で失敗するとソケットが閉じられずポートを握ったままになるため、失敗時は close してから再スローする
            runCatching {
                created.reuseAddress = true
                created.bind(java.net.InetSocketAddress(listenPort))
                created.soTimeout = timeoutMs
            }.onFailure { created.close() }.getOrThrow()
        }

    override fun receive(packet: DatagramPacket) = socket.receive(packet)

    override fun send(
        data: ByteArray,
        address: String,
        port: Int,
    ) {
        val addr = InetAddress.getByName(address)
        socket.send(DatagramPacket(data, data.size, addr, port))
    }

    override fun close() = socket.close()
}
