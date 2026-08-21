package kurou.kodriver.domain.model

enum class WheelIndex { FRONT_LEFT, FRONT_RIGHT, REAR_LEFT, REAR_RIGHT }

data class LmuWindowsTyreWheelData(
    val surfaceTemperature: CelsiusReading,
    val carcassTemperature: CelsiusReading,
    val brakeTemperature: CelsiusReading,
    val pressureKpa: Double,
    val wear: LmuWindowsTyreWearRatio,
)

data class LmuWindowsTyreData(
    val wheels: Map<WheelIndex, LmuWindowsTyreWheelData>,
)
