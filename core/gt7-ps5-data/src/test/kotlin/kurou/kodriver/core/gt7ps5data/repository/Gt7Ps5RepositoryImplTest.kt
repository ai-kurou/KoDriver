package kurou.kodriver.core.gt7ps5data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.core.gt7ps5data.mapper.Gt7Ps5Mapper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Gt7Ps5RepositoryImplTest {

    private fun makePacket(lapCount: Short = 1, lapsInRace: Short = 10): ByteBuffer {
        val buf = ByteBuffer.allocate(0x170).order(ByteOrder.LITTLE_ENDIAN)
        buf.putShort(0x74, lapCount)
        buf.putShort(0x76, lapsInRace)
        buf.putInt(0x78, -1)
        buf.putFloat(0x44, 50f)
        buf.putFloat(0x48, 100f)
        return buf
    }

    @Test
    fun `telemetryStreamはパケットをGt7Ps5TelemetryDataに変換して流す`() = runBlocking {
        val packet = makePacket(lapCount = 3, lapsInRace = 10)
        val source = FakeGt7Ps5PacketSource(flowOf(packet))
        val repo = Gt7Ps5RepositoryImpl(source)

        val result = repo.telemetryStream().first()

        assertEquals(Gt7Ps5Mapper.map(packet), result)
    }

    @Test
    fun `telemetryStreamは複数パケットを順番通りに流す`() = runBlocking {
        val p1 = makePacket(lapCount = 1)
        val p2 = makePacket(lapCount = 2)
        val source = FakeGt7Ps5PacketSource(flowOf(p1, p2))
        val repo = Gt7Ps5RepositoryImpl(source)

        val results = buildList { repo.telemetryStream().collect { add(it) } }

        assertEquals(listOf(Gt7Ps5Mapper.map(p1), Gt7Ps5Mapper.map(p2)), results)
    }

    @Test
    fun `最終パケット受信からタイムアウト未満であれば接続中と判定する`() = runBlocking {
        val now = 10_000L
        val source = FakeGt7Ps5PacketSource(flowOf(), lastReceivedAt = now - 4_999L)
        val repo = Gt7Ps5RepositoryImpl(source, currentTimeMillis = { now })

        assertTrue(repo.isConnected())
    }

    @Test
    fun `最終パケット受信からタイムアウト以上経過していれば未接続と判定する`() = runBlocking {
        val now = 10_000L
        val source = FakeGt7Ps5PacketSource(flowOf(), lastReceivedAt = now - 5_000L)
        val repo = Gt7Ps5RepositoryImpl(source, currentTimeMillis = { now })

        assertFalse(repo.isConnected())
    }

    @Test
    fun `パケットを一度も受信していなければ未接続と判定する`() = runBlocking {
        val source = FakeGt7Ps5PacketSource(flowOf(), lastReceivedAt = 0L)
        val repo = Gt7Ps5RepositoryImpl(source, currentTimeMillis = { 10_000L })

        assertFalse(repo.isConnected())
    }
}
