package kurou.kodriver.feature.acewindowsdebugstatedetail

import kurou.kodriver.domain.model.DebugStateCardKey

internal val defaultDebugStateCardOrder =
    listOf(
        DebugStateCardKey.SIMULATOR,
        DebugStateCardKey.VEHICLE_LOCATION,
        DebugStateCardKey.FLAG_INFO,
        DebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
        DebugStateCardKey.BEST_LAP,
        DebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
        DebugStateCardKey.FUEL_CONSUMPTION,
    )

data class AceWindowsDebugStateDetailUiState(
    val aceWindows: AceWindowsDebugState = AceWindowsDebugState(),
    val enabledCardKeys: Set<DebugStateCardKey> = setOf(DebugStateCardKey.SIMULATOR),
    val cardOrder: List<DebugStateCardKey> = defaultDebugStateCardOrder,
)
