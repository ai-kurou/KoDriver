package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.PressureKpa
import kurou.kodriver.domain.model.WheelIndex

@Serializable
data class LmuWindowsTyreWheelData(
    val surfaceTemperature: CelsiusReading,
    val carcassTemperature: CelsiusReading,
    val brakeTemperature: CelsiusReading,
    val pressureKpa: PressureKpa,
    val wear: LmuWindowsTyreWearRatio,
)

@Serializable
data class LmuWindowsTyreData(
    val wheels: Map<WheelIndex, LmuWindowsTyreWheelData>,
)
