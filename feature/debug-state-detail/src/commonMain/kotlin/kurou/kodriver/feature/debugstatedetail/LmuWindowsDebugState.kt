package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData

data class LmuWindowsDebugState(
    val raceFlags: LmuWindowsRaceFlagsData? = null,
    val virtualEnergy: LmuWindowsVirtualEnergyData? = null,
    val telemetry: LmuWindowsTelemetryData? = null,
    val pitStatus: LmuWindowsPitStatusData? = null,
    val sideBySideDurations: LmuWindowsSideBySideDurations? = null,
    val vehicleApproach: LmuWindowsVehicleApproachData? = null,
    val tyreCarcassTemperature: LmuWindowsTyreCarcassTemperatureData? = null,
    val brakeWear: LmuWindowsBrakeWearRemainingData? = null,
    val brakeTemperature: LmuWindowsBrakeTemperatureData? = null,
    val vehicleClass: LmuWindowsVehicleClassData? = null,
    val vehicleDamage: LmuWindowsVehicleDamageData? = null,
    val tyreDetached: LmuWindowsTyreDetachedData? = null,
)

internal val lmuWindowsSupportedCardKeys =
    setOf(
        DebugStateCardKey.SIMULATOR,
        DebugStateCardKey.VEHICLE_CLASS,
        DebugStateCardKey.VEHICLE_LOCATION,
        DebugStateCardKey.FLAG_INFO,
        DebugStateCardKey.GAME_PHASE,
        DebugStateCardKey.SESSION,
        DebugStateCardKey.YELLOW_FLAG_STATE,
        DebugStateCardKey.CURRENT_LAP,
        DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
        DebugStateCardKey.BEST_LAP,
        DebugStateCardKey.TYRE_TEMPERATURE,
        DebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
        DebugStateCardKey.BRAKE_TEMPERATURE,
        DebugStateCardKey.BRAKE_WEAR,
        DebugStateCardKey.TYRE_WEAR,
        DebugStateCardKey.FUEL_CONSUMPTION,
        DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
        DebugStateCardKey.VEHICLE_DAMAGE,
    )
