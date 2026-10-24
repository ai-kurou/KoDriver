package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsRemainingFuelPreferences(
    @ProtoNumber(1) val thresholdPercentage: Int = LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
    @ProtoNumber(2) val readoutText: String = LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
)
