package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.core.model.AceWindowsFlagData
import kurou.kodriver.core.model.AceWindowsFuelData
import kurou.kodriver.core.model.AceWindowsStatusData
import kurou.kodriver.core.model.DebugStateCardKey
import kurou.kodriver.core.model.Gt7Ps5TelemetryData
import kurou.kodriver.core.model.Gt7Ps5VehicleClassData
import kurou.kodriver.core.model.LmuWindowsPitStatusData
import kurou.kodriver.core.model.LmuWindowsRaceFlagsData
import kurou.kodriver.core.model.LmuWindowsTelemetryData
import kurou.kodriver.core.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.core.model.LmuWindowsVehicleApproachData
import kurou.kodriver.core.model.LmuWindowsVehicleClassData
import kurou.kodriver.core.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.core.model.Simulator

internal val defaultDebugStateCardOrder =
    listOf(
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
        DebugStateCardKey.TYRE_WEAR,
        DebugStateCardKey.FUEL_CONSUMPTION,
        DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
    )

data class DebugStateDetailUiState(
    val selectedSimulator: Simulator? = null,
    val raceFlags: LmuWindowsRaceFlagsData? = null,
    val virtualEnergy: LmuWindowsVirtualEnergyData? = null,
    val lmuWindowsTelemetry: LmuWindowsTelemetryData? = null,
    val gt7Ps5Telemetry: Gt7Ps5TelemetryData? = null,
    val aceWindowsFuel: AceWindowsFuelData? = null,
    val aceWindowsFlag: AceWindowsFlagData? = null,
    val aceWindowsStatus: AceWindowsStatusData? = null,
    val lmuWindowsPitStatus: LmuWindowsPitStatusData? = null,
    val vehicleApproach: LmuWindowsVehicleApproachData? = null,
    val tyreCarcassTemperature: LmuWindowsTyreCarcassTemperatureData? = null,
    val lmuWindowsVehicleClass: LmuWindowsVehicleClassData? = null,
    val gt7Ps5VehicleClass: Gt7Ps5VehicleClassData? = null,
    val enabledCardKeys: Set<DebugStateCardKey> = emptySet(),
    val cardOrder: List<DebugStateCardKey> = defaultDebugStateCardOrder,
)
