package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SKIP_FIRST_LAP_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_START_READOUT_TYPE_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_READOUT_TYPE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsVehicleApproachPreferences(
    @ProtoNumber(1) val skipFirstLap: Boolean = LMU_WINDOWS_VEHICLE_APPROACH_SKIP_FIRST_LAP_DEFAULT,
    @ProtoNumber(3) val startReadoutType: String = LMU_WINDOWS_VEHICLE_APPROACH_START_READOUT_TYPE_DEFAULT.id,
    @ProtoNumber(4) val enabledStates: Map<String, Boolean> = emptyMap(),
    @ProtoNumber(5) val sustainedReadoutType: String = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_READOUT_TYPE_DEFAULT.id,
)
