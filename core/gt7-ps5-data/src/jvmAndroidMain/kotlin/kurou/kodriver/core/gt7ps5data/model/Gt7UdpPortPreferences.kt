package kurou.kodriver.core.gt7ps5data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class Gt7UdpPortPreferences(
    @ProtoNumber(1) val port: Int = 33740,
)
