package kurou.kodriver.core.gt7ps5data.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Gt7Ps5UdpSourceTest {

    /**
     * GT7 パケット暗号化の構築ルール:
     *   - iv1 を offset 0x40 に LE4 バイトで書き込む（復号側がここを読み取る）
     *   - ノンス = [iv1 XOR 0xDEADBEEF (LE4), iv1 (LE4)]
     *   - 残りのフィールドは Salsa20(key, nonce) XOR 平文値 で埋める
     */
    private fun makeEncryptedPacket(
        magic: Int = Gt7Ps5UdpSource.MAGIC,
        lapCount: Short = 1,
        iv1: Int = 0,
    ): ByteArray {
        val iv2 = iv1 xor 0xDEADBEEF.toInt()
        val nonce = ByteArray(8)
        nonce.writeIntLE(0, iv2)
        nonce.writeIntLE(4, iv1)

        val keystream = Salsa20.decrypt(
            Gt7Ps5UdpSource.GT7_KEY,
            nonce,
            ByteArray(Gt7Ps5UdpSource.PACKET_MIN_SIZE),
        )

        val plain = ByteArray(Gt7Ps5UdpSource.PACKET_MIN_SIZE)
        val plainBuf = ByteBuffer.wrap(plain).order(ByteOrder.LITTLE_ENDIAN)
        plainBuf.putInt(Gt7Ps5UdpSource.MAGIC_OFFSET, magic)
        plainBuf.putShort(0x74, lapCount)

        val encrypted = ByteArray(Gt7Ps5UdpSource.PACKET_MIN_SIZE) { i ->
            (plain[i].toInt() xor keystream[i].toInt()).toByte()
        }
        // 復号側が iv1 として読み取る位置に iv1 を平文で埋め込む
        encrypted.writeIntLE(Gt7Ps5UdpSource.IV_OFFSET, iv1)

        return encrypted
    }

    private fun makeSource(socket: FakeUdpSocket): Gt7Ps5UdpSource = Gt7Ps5UdpSource(
        ps5Address = "192.168.1.100",
        socketFactory = { socket },
        scope = CoroutineScope(SupervisorJob()),
    )

    @Test
    fun `起動時にハートビートを送信する`() = runBlocking {
        val socket = FakeUdpSocket()
        socket.enqueuePacket(makeEncryptedPacket())
        val source = makeSource(socket)

        source.packetFlow.first()

        val first = socket.sentPackets.first()
        assertEquals(Gt7Ps5UdpSource.HEARTBEAT_PAYLOAD.toList(), first.data.toList())
        assertEquals("192.168.1.100", first.address)
        assertEquals(Gt7Ps5UdpSource.SEND_PORT, first.port)
    }

    @Test
    fun `正常パケットを受信するとByteBufferをemitする`() = runBlocking {
        val socket = FakeUdpSocket()
        socket.enqueuePacket(makeEncryptedPacket(lapCount = 3))
        val source = makeSource(socket)

        val result = source.packetFlow.first()

        assertEquals(Gt7Ps5UdpSource.MAGIC, result.getInt(Gt7Ps5UdpSource.MAGIC_OFFSET))
        assertEquals(3.toShort(), result.getShort(0x74))
    }

    @Test
    fun `マジックバイト不一致のパケットはemitしない`() = runBlocking {
        val socket = FakeUdpSocket()
        socket.enqueuePacket(makeEncryptedPacket(magic = 0xDEADBEEF.toInt()))
        socket.enqueuePacket(makeEncryptedPacket(lapCount = 5))
        val source = makeSource(socket)

        val result = source.packetFlow.first()

        assertEquals(5.toShort(), result.getShort(0x74))
    }

    @Test
    fun `パケットサイズが不足している場合はemitしない`() = runBlocking {
        val socket = FakeUdpSocket()
        socket.enqueuePacket(ByteArray(10))
        socket.enqueuePacket(makeEncryptedPacket(lapCount = 7))
        val source = makeSource(socket)

        val result = source.packetFlow.first()

        assertEquals(7.toShort(), result.getShort(0x74))
    }

    @Test
    fun `タイムアウト発生時にハートビートを再送する`() = runBlocking {
        val socket = FakeUdpSocket()
        socket.enqueueTimeout()
        socket.enqueuePacket(makeEncryptedPacket())
        val source = makeSource(socket)

        source.packetFlow.first()

        // 起動時 + タイムアウト後の最低2回ハートビートが送信されている
        // (collection 後もキャンセル完了まで追加送信があるため >= で検証する)
        assertTrue(
            socket.sentPackets.count { it.data.toList() == Gt7Ps5UdpSource.HEARTBEAT_PAYLOAD.toList() } >= 2,
        )
    }

    @Test
    fun `HEARTBEAT_INTERVAL_PACKETSごとにハートビートを再送する`() = runBlocking {
        val interval = Gt7Ps5UdpSource.HEARTBEAT_INTERVAL_PACKETS
        val socket = FakeUdpSocket()
        repeat(interval + 1) { i ->
            socket.enqueuePacket(makeEncryptedPacket(lapCount = i.toShort()))
        }
        val source = makeSource(socket)

        source.packetFlow.take(interval + 1).toList()

        // 起動時1回 + interval到達後1回の最低2回
        assertTrue(socket.sentPackets.size >= 2)
    }

    @Test
    fun `flowキャンセル時にソケットがcloseされる`() = runBlocking {
        val socket = FakeUdpSocket()
        socket.enqueuePacket(makeEncryptedPacket())
        val source = makeSource(socket)

        source.packetFlow.first()

        // WhileSubscribed が IO スレッドへキャンセルを伝播するまで最大 2 秒待機する
        val deadline = System.currentTimeMillis() + 2_000L
        while (!socket.closed && System.currentTimeMillis() < deadline) {
            delay(10)
        }

        assertTrue(socket.closed)
    }
}

private fun ByteArray.writeIntLE(offset: Int, value: Int) {
    this[offset] = (value and 0xFF).toByte()
    this[offset + 1] = ((value ushr 8) and 0xFF).toByte()
    this[offset + 2] = ((value ushr 16) and 0xFF).toByte()
    this[offset + 3] = ((value ushr 24) and 0xFF).toByte()
}
