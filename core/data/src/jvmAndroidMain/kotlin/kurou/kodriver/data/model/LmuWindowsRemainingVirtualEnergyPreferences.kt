package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_DEFAULT_THRESHOLD_PERCENTAGE

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsRemainingVirtualEnergyPreferences(
    @ProtoNumber(1) val thresholdPercentage: Int = LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_DEFAULT_THRESHOLD_PERCENTAGE,
)
