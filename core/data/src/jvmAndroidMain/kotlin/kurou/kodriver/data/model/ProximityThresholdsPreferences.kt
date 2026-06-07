package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class ProximityThresholdsPreferences(
    @ProtoNumber(1) val longitudinalThresholdMeters: Double = 1.0,
    @ProtoNumber(2) val lateralThresholdMeters: Double = 5.0,
)
