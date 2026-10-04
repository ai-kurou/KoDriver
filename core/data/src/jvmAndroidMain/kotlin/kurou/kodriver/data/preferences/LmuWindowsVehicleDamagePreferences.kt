package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_DAMAGE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_DAMAGE_PART_DETACHED_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_DAMAGE_TYRE_DETACHED_READOUT_TEXT_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsVehicleDamagePreferences(
    @ProtoNumber(1) val enabledStates: Map<String, Boolean> = emptyMap(),
    @ProtoNumber(2) val overheatReadoutText: String = LMU_WINDOWS_VEHICLE_DAMAGE_OVERHEAT_READOUT_TEXT_DEFAULT,
    @ProtoNumber(3) val partDetachedReadoutText: String = LMU_WINDOWS_VEHICLE_DAMAGE_PART_DETACHED_READOUT_TEXT_DEFAULT,
    @ProtoNumber(4) val tyreDetachedReadoutText: String = LMU_WINDOWS_VEHICLE_DAMAGE_TYRE_DETACHED_READOUT_TEXT_DEFAULT,
)
