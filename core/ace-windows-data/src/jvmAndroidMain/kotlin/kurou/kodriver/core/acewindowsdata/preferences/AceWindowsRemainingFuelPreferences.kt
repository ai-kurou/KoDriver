package kurou.kodriver.core.acewindowsdata.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class AceWindowsRemainingFuelPreferences(
    @ProtoNumber(1) val thresholdPercentage: Int = ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
    @ProtoNumber(2) val readoutText: String = ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
)
