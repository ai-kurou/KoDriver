package kurou.kodriver.core.gt7ps5data.datasource

import kotlinx.coroutines.flow.Flow
import java.nio.ByteBuffer

internal interface Gt7Ps5PacketSource {
    val packetFlow: Flow<ByteBuffer>
}
