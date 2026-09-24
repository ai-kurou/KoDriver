package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class FlagReadoutTextPreferences(
    @ProtoNumber(1) val sectorYellowFlagText: String = READOUT_CUSTOM_TEXT_DEFAULT,
)
