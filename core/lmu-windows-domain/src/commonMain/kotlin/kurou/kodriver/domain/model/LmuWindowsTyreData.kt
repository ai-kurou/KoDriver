package kurou.kodriver.domain.model

data class LmuWindowsTyreWheelData(
    val surfaceTemperature: CelsiusReading,
    val carcassTemperature: CelsiusReading,
    val brakeTemperature: CelsiusReading,
    val pressureKpa: LmuWindowsPressureKpa,
    val wear: LmuWindowsTyreWearRatio,
)

data class LmuWindowsTyreData(
    val wheels: Map<WheelIndex, LmuWindowsTyreWheelData>,
)
