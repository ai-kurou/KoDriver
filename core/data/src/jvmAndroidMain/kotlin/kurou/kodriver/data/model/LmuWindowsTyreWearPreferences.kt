package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_DEFAULT_THRESHOLD_PERCENTAGE

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsTyreWearPreferences(
    @ProtoNumber(1) val thresholdPercentage: Int = LMU_WINDOWS_TYRE_WEAR_DEFAULT_THRESHOLD_PERCENTAGE,
)
