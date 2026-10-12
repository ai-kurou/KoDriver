package kurou.kodriver.feature.acewindowsdebugstatedetail

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
    val aceWindows: AceWindowsDebugState = AceWindowsDebugState(),
    val enabledCardKeys: Set<AceWindowsDebugStateCardKey> = setOf(AceWindowsDebugStateCardKey.SIMULATOR),
    val cardOrder: List<AceWindowsDebugStateCardKey> = defaultDebugStateCardOrder,
)
