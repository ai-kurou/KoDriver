package kurou.kodriver.feature.acewindowsdebugstatedetail

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

internal val defaultDebugStateCardOrder =
    listOf(
        AceWindowsDebugStateCardKey.SIMULATOR,
        AceWindowsDebugStateCardKey.VEHICLE_LOCATION,
        AceWindowsDebugStateCardKey.FLAG_INFO,
        AceWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
        AceWindowsDebugStateCardKey.BEST_LAP,
        AceWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
        AceWindowsDebugStateCardKey.FUEL_CONSUMPTION,
    )

internal data class AceWindowsDebugStateDetailUiState(
    val debugOverlayText: String = "",
    val debugOverlayKeys: List<ReadoutItemKey> = ReadoutItemKey.entriesOf(Simulator.AceWindows),
    val debugOverlaySelectedKey: ReadoutItemKey = ReadoutItemKey.entriesOf(Simulator.AceWindows).first(),
    val aceWindows: AceWindowsDebugState = AceWindowsDebugState(),
    val enabledCardKeys: Set<AceWindowsDebugStateCardKey> = setOf(AceWindowsDebugStateCardKey.SIMULATOR),
    val cardOrder: List<AceWindowsDebugStateCardKey> = defaultDebugStateCardOrder,
)
