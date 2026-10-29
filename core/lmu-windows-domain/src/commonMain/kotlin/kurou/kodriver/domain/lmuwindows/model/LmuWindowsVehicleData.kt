package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable
import kotlin.math.sqrt

@Serializable
data class LmuWindowsVehicleData(
    val localVelocityX: Double,
    val localVelocityY: Double,
    val localVelocityZ: Double,
    val positionX: Double,
    val positionY: Double,
    val positionZ: Double,
) {
    val speedMs: Double
        get() =
            sqrt(
                localVelocityX * localVelocityX +
                    localVelocityY * localVelocityY +
                    localVelocityZ * localVelocityZ,
            )
}
