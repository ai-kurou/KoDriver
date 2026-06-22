package kurou.kodriver.core.gt7ps5data.datasource

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class ConsoleAddressPreferences(
    @ProtoNumber(1) val address: String = "",
)
