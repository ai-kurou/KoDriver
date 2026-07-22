package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.Simulator

internal val defaultDebugStateCardOrder = listOf(
    DebugStateCardKey.SIMULATOR,
    DebugStateCardKey.FLAG_INFO,
    DebugStateCardKey.GAME_PHASE,
    DebugStateCardKey.SESSION,
    DebugStateCardKey.YELLOW_FLAG_STATE,
    DebugStateCardKey.CURRENT_LAP,
    DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
    DebugStateCardKey.BEST_LAP,
    DebugStateCardKey.TYRE_TEMPERATURE,
)

internal data class DebugStateDetailUiState(
    val selectedSimulator: Simulator? = null,
    val raceFlags: LmuWindowsRaceFlagsData? = null,
    val virtualEnergy: LmuWindowsVirtualEnergyData? = null,
    val lmuWindowsTelemetry: LmuWindowsTelemetryData? = null,
    val gt7Ps5Telemetry: Gt7Ps5TelemetryData? = null,
    val vehicleApproach: LmuWindowsVehicleApproachData? = null,
    val cardOrder: List<DebugStateCardKey> = defaultDebugStateCardOrder,
)
