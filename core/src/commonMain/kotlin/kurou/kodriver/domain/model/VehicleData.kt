package kurou.kodriver.domain.model

import kotlin.math.sqrt

data class VehicleData(
    val localVelocityX: Double,
    val localVelocityY: Double,
    val localVelocityZ: Double,
    val positionX: Double,
    val positionY: Double,
    val positionZ: Double,
) {
    val speedMs: Double
        get() = sqrt(localVelocityX * localVelocityX + localVelocityY * localVelocityY + localVelocityZ * localVelocityZ)

    val speedKmh: Double
        get() = speedMs * 3.6
}
