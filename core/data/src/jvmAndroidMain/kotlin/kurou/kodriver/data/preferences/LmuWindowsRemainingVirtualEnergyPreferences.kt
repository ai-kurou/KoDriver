package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsRemainingVirtualEnergyPreferences(
    @ProtoNumber(1) val thresholdPercentage: Int = LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT,
)
