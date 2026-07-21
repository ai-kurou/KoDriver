package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsVehicleApproachThresholdsPreferences(
    @ProtoNumber(1) val longitudinalThresholdMeters: Double = 5.0,
    @ProtoNumber(2) val lateralThresholdMeters: Double = 5.0,
    @ProtoNumber(3) val sustainedApproachDurationSeconds: Int =
        LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
)
