package kurou.kodriver.feature.gt7ps5debugstatedetail

import kurou.kodriver.domain.model.GT7_PS5_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.ReadoutItemKey

internal val defaultDebugStateCardOrder =
    listOf(
        Gt7Ps5DebugStateCardKey.SIMULATOR,
        Gt7Ps5DebugStateCardKey.VEHICLE_CLASS,
        Gt7Ps5DebugStateCardKey.CURRENT_LAP,
        Gt7Ps5DebugStateCardKey.BEST_LAP,
        Gt7Ps5DebugStateCardKey.TYRE_TEMPERATURE,
        Gt7Ps5DebugStateCardKey.FUEL_CONSUMPTION,
    )

internal data class Gt7Ps5DebugStateDetailUiState(
    val selectedReadoutItemKey: ReadoutItemKey =
        GT7_PS5_READOUT_ITEM_KEYS
            .filterIsInstance<ReadoutItemKey.TopLevel>()
            .first(),
    val debugReadoutText: String = "",
    val gt7Ps5: Gt7Ps5DebugState = Gt7Ps5DebugState(),
    val enabledCardKeys: Set<Gt7Ps5DebugStateCardKey> = setOf(Gt7Ps5DebugStateCardKey.SIMULATOR),
    val cardOrder: List<Gt7Ps5DebugStateCardKey> = defaultDebugStateCardOrder,
)
