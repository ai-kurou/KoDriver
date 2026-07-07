package kurou.kodriver.domain.model

enum class WheelIndex { FRONT_LEFT, FRONT_RIGHT, REAR_LEFT, REAR_RIGHT }

data class LmuWindowsTyreWheelData(
    val surfaceTemperatureK: Double,
    val carcassTemperatureK: Double,
    val brakeTemperatureC: Double,
    val pressureKpa: Double,
    val wear: Double,
)

data class LmuWindowsTyreData(
    val wheels: Map<WheelIndex, LmuWindowsTyreWheelData>,
)
