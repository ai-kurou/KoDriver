package kurou.kodriver.feature.gt7ps5debugstatedetail

import kurou.kodriver.domain.model.DebugStateCardKey

internal val defaultDebugStateCardOrder =
    listOf(
        DebugStateCardKey.SIMULATOR,
        DebugStateCardKey.VEHICLE_CLASS,
        DebugStateCardKey.CURRENT_LAP,
        DebugStateCardKey.BEST_LAP,
        DebugStateCardKey.TYRE_TEMPERATURE,
        DebugStateCardKey.FUEL_CONSUMPTION,
    )

data class Gt7Ps5DebugStateDetailUiState(
    val gt7Ps5: Gt7Ps5DebugState = Gt7Ps5DebugState(),
    val enabledCardKeys: Set<DebugStateCardKey> = setOf(DebugStateCardKey.SIMULATOR),
    val cardOrder: List<DebugStateCardKey> = defaultDebugStateCardOrder,
)
