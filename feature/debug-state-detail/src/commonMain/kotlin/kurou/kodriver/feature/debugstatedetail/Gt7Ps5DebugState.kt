package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5VehicleClassData

data class Gt7Ps5DebugState(
    val telemetry: Gt7Ps5TelemetryData? = null,
    val vehicleClass: Gt7Ps5VehicleClassData? = null,
)
