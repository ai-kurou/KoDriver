package kurou.kodriver.core.gt7ps5data.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5PacketSource
import java.nio.ByteBuffer

internal class FakeGt7Ps5PacketSource(
    private val flow: Flow<ByteBuffer>,
    private val lastReceivedAt: Long = 0L,
) : Gt7Ps5PacketSource {
    override val packetFlow: Flow<ByteBuffer> = flow
    override fun lastPacketReceivedAt(): Long = lastReceivedAt
}
