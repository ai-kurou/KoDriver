package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ReadoutPreferences(
    @ProtoNumber(1) val simulatorStates: Map<String, SimulatorReadoutState> = emptyMap(),
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SimulatorReadoutState(
    @ProtoNumber(1) val enabledStates: Map<String, Boolean> = emptyMap(),
    @ProtoNumber(2) val itemOrder: List<String> = emptyList(),
)
