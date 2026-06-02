package kurou.kodriver.domain.model

enum class WheelIndex { FRONT_LEFT, FRONT_RIGHT, REAR_LEFT, REAR_RIGHT }

data class TyreWheelData(
    val surfaceTemperatureK: Double,
    val brakeTemperatureC: Double,
    val pressureKpa: Double,
    val wear: Double,
)

data class TyreData(
    val wheels: Map<WheelIndex, TyreWheelData>,
)
