package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.DebugStateCardAccent
import kurou.kodriver.core.designsystem.DebugStateCardStyle
import kurou.kodriver.core.designsystem.DebugStateCardFrame as CommonDebugStateCardFrame

@Composable
internal fun DebugStateCardFrame(
    cardKey: LmuWindowsDebugStateCardKey,
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
        LmuWindowsDebugStateCardKey.SIMULATOR to DebugStateCardStyle("🎮", DebugStateCardAccent.PRIMARY),
        LmuWindowsDebugStateCardKey.VEHICLE_CLASS to DebugStateCardStyle("🏎", DebugStateCardAccent.PRIMARY),
        LmuWindowsDebugStateCardKey.VEHICLE_LOCATION to DebugStateCardStyle("⌖", DebugStateCardAccent.PRIMARY),
        LmuWindowsDebugStateCardKey.FLAG_INFO to DebugStateCardStyle("⚑", DebugStateCardAccent.TERTIARY),
        LmuWindowsDebugStateCardKey.GAME_PHASE to DebugStateCardStyle("🚦", DebugStateCardAccent.TERTIARY),
        LmuWindowsDebugStateCardKey.SESSION to DebugStateCardStyle("◷", DebugStateCardAccent.TERTIARY),
        LmuWindowsDebugStateCardKey.YELLOW_FLAG_STATE to DebugStateCardStyle("⚠", DebugStateCardAccent.TERTIARY),
        LmuWindowsDebugStateCardKey.CURRENT_LAP to DebugStateCardStyle("⏱", DebugStateCardAccent.TERTIARY),
        LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES to DebugStateCardStyle("↔", DebugStateCardAccent.TERTIARY),
        LmuWindowsDebugStateCardKey.CAR_GAP to DebugStateCardStyle("↕", DebugStateCardAccent.TERTIARY),
        LmuWindowsDebugStateCardKey.CLASS_CAR_GAP to DebugStateCardStyle("↕", DebugStateCardAccent.TERTIARY),
        LmuWindowsDebugStateCardKey.BEST_LAP to DebugStateCardStyle("🏆", DebugStateCardAccent.TERTIARY),
        LmuWindowsDebugStateCardKey.TYRE_TEMPERATURE to DebugStateCardStyle("🔥", DebugStateCardAccent.ERROR),
        LmuWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE to DebugStateCardStyle("◎", DebugStateCardAccent.ERROR),
        LmuWindowsDebugStateCardKey.BRAKE_TEMPERATURE to DebugStateCardStyle("◉", DebugStateCardAccent.ERROR),
        LmuWindowsDebugStateCardKey.BRAKE_WEAR to DebugStateCardStyle("◒", DebugStateCardAccent.ERROR),
        LmuWindowsDebugStateCardKey.TYRE_WEAR to DebugStateCardStyle("◴", DebugStateCardAccent.ERROR),
        LmuWindowsDebugStateCardKey.FUEL_CONSUMPTION to DebugStateCardStyle("⛽", DebugStateCardAccent.WARNING),
        LmuWindowsDebugStateCardKey.VIRTUAL_ENERGY to DebugStateCardStyle("⚡", DebugStateCardAccent.WARNING),
        LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS to DebugStateCardStyle("⚒", DebugStateCardAccent.WARNING),
        LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE to DebugStateCardStyle("💥", DebugStateCardAccent.ERROR),
    )

internal fun debugStateCardSymbol(cardKey: LmuWindowsDebugStateCardKey): String =
    debugStateCardStyles.getValue(cardKey).symbol
