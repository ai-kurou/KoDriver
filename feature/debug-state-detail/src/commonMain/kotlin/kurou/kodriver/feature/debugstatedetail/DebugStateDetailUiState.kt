package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5VehicleClassData
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.SELECTED_SIMULATOR_DEFAULT
import kurou.kodriver.domain.model.Simulator

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
        DebugStateCardKey.VEHICLE_DAMAGE,
    )

data class DebugStateDetailUiState(
    val selectedSimulator: Simulator = SELECTED_SIMULATOR_DEFAULT,
    val raceFlags: LmuWindowsRaceFlagsData? = null,
    val virtualEnergy: LmuWindowsVirtualEnergyData? = null,
    val lmuWindowsTelemetry: LmuWindowsTelemetryData? = null,
    val gt7Ps5Telemetry: Gt7Ps5TelemetryData? = null,
    val aceWindowsFuel: AceWindowsFuelData? = null,
    val aceWindowsFlag: AceWindowsFlagData? = null,
    val aceWindowsStatus: AceWindowsStatusData? = null,
    val aceWindowsBestLapTime: AceWindowsBestLapTimeData? = null,
    val lmuWindowsPitStatus: LmuWindowsPitStatusData? = null,
    val vehicleApproach: LmuWindowsVehicleApproachData? = null,
    val aceWindowsVehicleApproach: AceWindowsVehicleApproachData? = null,
    val tyreCarcassTemperature: LmuWindowsTyreCarcassTemperatureData? = null,
    val aceWindowsTyreCarcassTemperature: AceWindowsTyreCarcassTemperatureData? = null,
    val lmuWindowsVehicleClass: LmuWindowsVehicleClassData? = null,
    val gt7Ps5VehicleClass: Gt7Ps5VehicleClassData? = null,
    val vehicleDamage: LmuWindowsVehicleDamageData? = null,
    val enabledCardKeys: Set<DebugStateCardKey> = emptySet(),
    val cardOrder: List<DebugStateCardKey> = defaultDebugStateCardOrder,
)
