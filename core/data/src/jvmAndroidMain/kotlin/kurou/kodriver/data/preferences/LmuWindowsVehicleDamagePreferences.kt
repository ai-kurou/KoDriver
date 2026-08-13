package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsVehicleDamagePreferences(
    @ProtoNumber(1) val enabledStates: Map<String, Boolean> = emptyMap(),
)
