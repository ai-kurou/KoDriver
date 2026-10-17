package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.PressureKpa
import kurou.kodriver.domain.model.WheelIndex

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
