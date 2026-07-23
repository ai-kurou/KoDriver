package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_LAPS_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsRemainingVirtualEnergyLapsPreferences(
    @ProtoNumber(1) val remainingVirtualEnergyLaps: Int = LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_LAPS_DEFAULT,
)
