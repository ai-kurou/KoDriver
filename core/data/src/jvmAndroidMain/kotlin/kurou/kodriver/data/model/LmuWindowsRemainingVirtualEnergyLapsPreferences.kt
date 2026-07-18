package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsRemainingVirtualEnergyLapsPreferences(
    @ProtoNumber(1) val remainingVirtualEnergyLaps: Int = 3,
)
