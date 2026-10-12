package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

internal val defaultDebugStateCardOrder =
    listOf(
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
        LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
        LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE,
    )

data class LmuWindowsDebugStateDetailUiState(
    val selectedReadoutItemKey: ReadoutItemKey = ReadoutItemKey.topLevelEntriesOf(Simulator.LmuWindows).first(),
    val debugReadoutText: String = "",
    val lmuWindows: LmuWindowsDebugState = LmuWindowsDebugState(),
    val enabledCardKeys: Set<LmuWindowsDebugStateCardKey> = setOf(LmuWindowsDebugStateCardKey.SIMULATOR),
    val cardOrder: List<LmuWindowsDebugStateCardKey> = defaultDebugStateCardOrder,
)
