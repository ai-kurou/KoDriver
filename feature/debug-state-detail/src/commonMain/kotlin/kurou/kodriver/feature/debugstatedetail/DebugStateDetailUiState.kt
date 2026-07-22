package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.Simulator

internal val defaultDebugStateCardOrder = listOf(
    DebugStateCardKey.SIMULATOR,
    DebugStateCardKey.FLAG_INFO,
    DebugStateCardKey.GAME_PHASE,
    DebugStateCardKey.SESSION,
)

internal data class DebugStateDetailUiState(
    val selectedSimulator: Simulator? = null,
    val raceFlags: LmuWindowsRaceFlagsData? = null,
    val virtualEnergy: LmuWindowsVirtualEnergyData? = null,
    val cardOrder: List<DebugStateCardKey> = defaultDebugStateCardOrder,
)
