package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.Simulator

internal val defaultDebugStateCardOrder = listOf(
    DebugStateCardKey.SIMULATOR,
    DebugStateCardKey.FLAG_INFO,
    DebugStateCardKey.GAME_PHASE,
)

internal data class DebugStateDetailUiState(
    val selectedSimulator: Simulator? = null,
    val raceFlags: LmuWindowsRaceFlagsData? = null,
    val cardOrder: List<DebugStateCardKey> = defaultDebugStateCardOrder,
)
