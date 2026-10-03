package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsPitTimingPreferences(
    @ProtoNumber(1) val virtualEnergyLaps: Int = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT,
    @ProtoNumber(2) val tyreWearLaps: Int = LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT,
    @ProtoNumber(3) val enabledStates: Map<String, Boolean> = emptyMap(),
    @ProtoNumber(4) val virtualEnergyReadoutText: String = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT,
    @ProtoNumber(5) val virtualEnergyImminentReadoutText: String =
        LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT,
)
