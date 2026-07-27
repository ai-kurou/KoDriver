package kurou.kodriver.core.gt7ps5data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.GT7_PS5_UDP_PORT_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class Gt7Ps5UdpPortPreferences(
    @ProtoNumber(1) val port: Int = GT7_PS5_UDP_PORT_DEFAULT,
)
