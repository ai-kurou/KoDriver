package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsNearbyVehicleData(
    val vehicleId: Int,
    // 正=前方、負=後方
    val longitudinalDistanceMeters: Double,
    // 正=右、負=左
    val lateralDistanceMeters: Double,
)

@Serializable
data class LmuWindowsNearbyVehiclesData(
    val vehicles: List<LmuWindowsNearbyVehicleData>,
)
