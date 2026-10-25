package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData

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
    val fuelBaseline: LmuWindowsFuelBaseline? = null,
)

internal val lmuWindowsSupportedCardKeys =
    setOf(
        LmuWindowsDebugStateCardKey.SIMULATOR,
        LmuWindowsDebugStateCardKey.VEHICLE_CLASS,
        LmuWindowsDebugStateCardKey.VEHICLE_LOCATION,
        LmuWindowsDebugStateCardKey.FLAG_INFO,
        LmuWindowsDebugStateCardKey.GAME_PHASE,
        LmuWindowsDebugStateCardKey.SESSION,
        LmuWindowsDebugStateCardKey.YELLOW_FLAG_STATE,
        LmuWindowsDebugStateCardKey.CURRENT_LAP,
        LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
        LmuWindowsDebugStateCardKey.BEST_LAP,
        LmuWindowsDebugStateCardKey.TYRE_TEMPERATURE,
        LmuWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
        LmuWindowsDebugStateCardKey.BRAKE_TEMPERATURE,
        LmuWindowsDebugStateCardKey.BRAKE_WEAR,
        LmuWindowsDebugStateCardKey.TYRE_WEAR,
        LmuWindowsDebugStateCardKey.FUEL_CONSUMPTION,
        LmuWindowsDebugStateCardKey.VIRTUAL_ENERGY,
        LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
        LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE,
    )
