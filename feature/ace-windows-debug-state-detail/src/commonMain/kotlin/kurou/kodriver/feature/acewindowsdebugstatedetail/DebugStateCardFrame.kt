package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.core.designsystem.DebugStateCardFrame as CommonDebugStateCardFrame

@Composable
internal fun DebugStateCardFrame(
    cardKey: AceWindowsDebugStateCardKey,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val accent =
        when (cardKey) {
            AceWindowsDebugStateCardKey.SIMULATOR, AceWindowsDebugStateCardKey.VEHICLE_LOCATION -> colors.primary
            AceWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE -> colors.error
            AceWindowsDebugStateCardKey.FUEL_CONSUMPTION -> KoDriverExtendedColors.current.onWarningContainer
            else -> colors.tertiary
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
        AceWindowsDebugStateCardKey.SIMULATOR to "🎮",
        AceWindowsDebugStateCardKey.VEHICLE_LOCATION to "⌖",
        AceWindowsDebugStateCardKey.FLAG_INFO to "⚑",
        AceWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES to "↔",
        AceWindowsDebugStateCardKey.BEST_LAP to "🏆",
        AceWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE to "◎",
        AceWindowsDebugStateCardKey.FUEL_CONSUMPTION to "⛽",
        AceWindowsDebugStateCardKey.CAR_GAP to "↕",
    )

internal fun debugStateCardSymbol(cardKey: AceWindowsDebugStateCardKey): String =
    debugStateCardSymbols.getValue(cardKey)
