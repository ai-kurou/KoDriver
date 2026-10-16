package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.core.designsystem.DebugStateCardFrame as CommonDebugStateCardFrame

@Composable
internal fun DebugStateCardFrame(
    cardKey: LmuWindowsDebugStateCardKey,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val accent =
        when (cardKey) {
            LmuWindowsDebugStateCardKey.SIMULATOR, LmuWindowsDebugStateCardKey.VEHICLE_CLASS,
            LmuWindowsDebugStateCardKey.VEHICLE_LOCATION,
            -> {
                colors.primary
            }

            LmuWindowsDebugStateCardKey.TYRE_TEMPERATURE, LmuWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
            LmuWindowsDebugStateCardKey.BRAKE_TEMPERATURE,
            LmuWindowsDebugStateCardKey.BRAKE_WEAR, LmuWindowsDebugStateCardKey.TYRE_WEAR,
            LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE,
            -> {
                colors.error
            }

            LmuWindowsDebugStateCardKey.FUEL_CONSUMPTION, LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS -> {
                KoDriverExtendedColors.current.onWarningContainer
            }

            else -> {
                colors.tertiary
            }
        }
    CommonDebugStateCardFrame(
        title = title,
        symbol = debugStateCardSymbol(cardKey),
        accent = accent,
        modifier = modifier,
        content = content,
    )
}

private val debugStateCardSymbols =
    mapOf(
        LmuWindowsDebugStateCardKey.SIMULATOR to "🎮",
        LmuWindowsDebugStateCardKey.VEHICLE_CLASS to "🏎",
        LmuWindowsDebugStateCardKey.VEHICLE_LOCATION to "⌖",
        LmuWindowsDebugStateCardKey.FLAG_INFO to "⚑",
        LmuWindowsDebugStateCardKey.GAME_PHASE to "🚦",
        LmuWindowsDebugStateCardKey.SESSION to "◷",
        LmuWindowsDebugStateCardKey.YELLOW_FLAG_STATE to "⚠",
        LmuWindowsDebugStateCardKey.CURRENT_LAP to "⏱",
        LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES to "↔",
        LmuWindowsDebugStateCardKey.BEST_LAP to "🏆",
        LmuWindowsDebugStateCardKey.TYRE_TEMPERATURE to "🔥",
        LmuWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE to "◎",
        LmuWindowsDebugStateCardKey.BRAKE_TEMPERATURE to "◉",
        LmuWindowsDebugStateCardKey.BRAKE_WEAR to "◒",
        LmuWindowsDebugStateCardKey.TYRE_WEAR to "◴",
        LmuWindowsDebugStateCardKey.FUEL_CONSUMPTION to "⛽",
        LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS to "⚒",
        LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE to "💥",
    )

internal fun debugStateCardSymbol(cardKey: LmuWindowsDebugStateCardKey): String =
    debugStateCardSymbols.getValue(cardKey)
