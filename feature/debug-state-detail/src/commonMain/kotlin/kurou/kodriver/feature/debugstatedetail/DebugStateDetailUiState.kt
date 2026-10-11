package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.DebugStateCardKey
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
        DebugStateCardKey.BRAKE_TEMPERATURE,
        DebugStateCardKey.BRAKE_WEAR,
        DebugStateCardKey.TYRE_WEAR,
        DebugStateCardKey.FUEL_CONSUMPTION,
        DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
        DebugStateCardKey.VEHICLE_DAMAGE,
    )

data class DebugStateDetailUiState(
    val selectedSimulator: Simulator = SELECTED_SIMULATOR_DEFAULT,
    val lmuWindows: LmuWindowsDebugState = LmuWindowsDebugState(),
    val gt7Ps5: Gt7Ps5DebugState = Gt7Ps5DebugState(),
    val aceWindows: AceWindowsDebugState = AceWindowsDebugState(),
    val enabledCardKeys: Set<DebugStateCardKey> = emptySet(),
    val cardOrder: List<DebugStateCardKey> = defaultDebugStateCardOrder,
)
