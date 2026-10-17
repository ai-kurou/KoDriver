package kurou.kodriver.feature.gt7ps5debugstatedetail

import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5VehicleClassData

data class Gt7Ps5DebugState(
    val telemetry: Gt7Ps5TelemetryData? = null,
    val vehicleClass: Gt7Ps5VehicleClassData? = null,
)

internal val gt7Ps5SupportedCardKeys =
    setOf(
        Gt7Ps5DebugStateCardKey.SIMULATOR,
        Gt7Ps5DebugStateCardKey.VEHICLE_CLASS,
        Gt7Ps5DebugStateCardKey.CURRENT_LAP,
        Gt7Ps5DebugStateCardKey.BEST_LAP,
        Gt7Ps5DebugStateCardKey.TYRE_TEMPERATURE,
        Gt7Ps5DebugStateCardKey.FUEL_CONSUMPTION,
    )
