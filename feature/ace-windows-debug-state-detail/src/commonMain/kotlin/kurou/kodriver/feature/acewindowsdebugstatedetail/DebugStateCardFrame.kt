package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.DebugStateCardAccent
import kurou.kodriver.core.designsystem.DebugStateCardStyle
import kurou.kodriver.core.designsystem.DebugStateCardFrame as CommonDebugStateCardFrame

@Composable
internal fun DebugStateCardFrame(
    cardKey: AceWindowsDebugStateCardKey,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    CommonDebugStateCardFrame(
        title = title,
        style = debugStateCardStyles.getValue(cardKey),
        modifier = modifier,
        content = content,
    )
}

private val debugStateCardStyles =
    mapOf(
        AceWindowsDebugStateCardKey.SIMULATOR to DebugStateCardStyle("🎮", DebugStateCardAccent.PRIMARY),
        AceWindowsDebugStateCardKey.VEHICLE_LOCATION to DebugStateCardStyle("⌖", DebugStateCardAccent.PRIMARY),
        AceWindowsDebugStateCardKey.FLAG_INFO to DebugStateCardStyle("⚑", DebugStateCardAccent.TERTIARY),
        AceWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES to DebugStateCardStyle("↔", DebugStateCardAccent.TERTIARY),
        AceWindowsDebugStateCardKey.BEST_LAP to DebugStateCardStyle("🏆", DebugStateCardAccent.TERTIARY),
        AceWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE to DebugStateCardStyle("◎", DebugStateCardAccent.ERROR),
        AceWindowsDebugStateCardKey.FUEL_CONSUMPTION to DebugStateCardStyle("⛽", DebugStateCardAccent.WARNING),
        AceWindowsDebugStateCardKey.CAR_GAP to DebugStateCardStyle("↕", DebugStateCardAccent.TERTIARY),
    )

internal fun debugStateCardSymbol(cardKey: AceWindowsDebugStateCardKey): String =
    debugStateCardStyles.getValue(cardKey).symbol
