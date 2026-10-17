package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.PressureKpa

enum class WheelIndex { FRONT_LEFT, FRONT_RIGHT, REAR_LEFT, REAR_RIGHT }

data class LmuWindowsTyreWheelData(
    val surfaceTemperature: CelsiusReading,
    val carcassTemperature: CelsiusReading,
    val brakeTemperature: CelsiusReading,
    val pressureKpa: PressureKpa,
    val wear: LmuWindowsTyreWearRatio,
)

data class LmuWindowsTyreData(
    val wheels: Map<WheelIndex, LmuWindowsTyreWheelData>,
)
