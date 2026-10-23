package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsFlagPreferences(
    @ProtoNumber(1) val enabledStates: Map<String, Boolean> = emptyMap(),
)
