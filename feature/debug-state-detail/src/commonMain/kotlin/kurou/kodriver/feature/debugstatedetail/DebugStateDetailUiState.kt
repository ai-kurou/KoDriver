package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsNearbyVehiclesData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.Simulator

internal val defaultDebugStateCardOrder = listOf(
    DebugStateCardKey.SIMULATOR,
    DebugStateCardKey.FLAG_INFO,
    DebugStateCardKey.GAME_PHASE,
    DebugStateCardKey.SESSION,
    DebugStateCardKey.YELLOW_FLAG_STATE,
    DebugStateCardKey.CURRENT_LAP,
    DebugStateCardKey.NEARBY_VEHICLES,
)

internal data class DebugStateDetailUiState(
    val selectedSimulator: Simulator? = null,
    val raceFlags: LmuWindowsRaceFlagsData? = null,
    val virtualEnergy: LmuWindowsVirtualEnergyData? = null,
    val lmuWindowsTelemetry: LmuWindowsTelemetryData? = null,
    val gt7Ps5Telemetry: Gt7Ps5TelemetryData? = null,
    val nearbyVehicles: LmuWindowsNearbyVehiclesData? = null,
    val cardOrder: List<DebugStateCardKey> = defaultDebugStateCardOrder,
)
