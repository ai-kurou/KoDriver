package kurou.kodriver.feature.acewindowsdebugstatedetail

import kurou.kodriver.domain.model.ACE_WINDOWS_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.ReadoutItemKey

internal val defaultDebugStateCardOrder =
    listOf(
        AceWindowsDebugStateCardKey.SIMULATOR,
        AceWindowsDebugStateCardKey.VEHICLE_LOCATION,
        AceWindowsDebugStateCardKey.FLAG_INFO,
        AceWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES,
        AceWindowsDebugStateCardKey.BEST_LAP,
        AceWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
        AceWindowsDebugStateCardKey.FUEL_CONSUMPTION,
        AceWindowsDebugStateCardKey.CAR_GAP,
    )

internal data class AceWindowsDebugStateDetailUiState(
    val selectedReadoutItemKey: ReadoutItemKey =
        ACE_WINDOWS_READOUT_ITEM_KEYS
            .filterIsInstance<ReadoutItemKey.TopLevel>()
            .first(),
    val debugReadoutText: String = "",
    val aceWindows: AceWindowsDebugState = AceWindowsDebugState(),
    val enabledCardKeys: Set<AceWindowsDebugStateCardKey> = setOf(AceWindowsDebugStateCardKey.SIMULATOR),
    val cardOrder: List<AceWindowsDebugStateCardKey> = defaultDebugStateCardOrder,
)
