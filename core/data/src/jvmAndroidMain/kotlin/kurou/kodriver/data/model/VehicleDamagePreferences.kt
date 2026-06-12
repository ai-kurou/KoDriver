package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class VehicleDamagePreferences(
    @ProtoNumber(1) val enabledStates: Map<String, Boolean> = emptyMap(),
)
