package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.core.designsystem.DebugStateCardFrame as CommonDebugStateCardFrame

@Composable
internal fun DebugStateCardFrame(
    cardKey: Gt7Ps5DebugStateCardKey,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val accent =
        when (cardKey) {
            Gt7Ps5DebugStateCardKey.SIMULATOR, Gt7Ps5DebugStateCardKey.VEHICLE_CLASS -> colors.primary
            Gt7Ps5DebugStateCardKey.TYRE_TEMPERATURE -> colors.error
            Gt7Ps5DebugStateCardKey.FUEL_CONSUMPTION -> KoDriverExtendedColors.current.onWarningContainer
            else -> colors.tertiary
        }
    CommonDebugStateCardFrame(
        title = title,
        symbol = debugStateCardSymbol(cardKey),
        accent = accent,
        modifier = modifier,
        showHandle = false,
        content = content,
    )
}

private val debugStateCardSymbols =
    mapOf(
        Gt7Ps5DebugStateCardKey.SIMULATOR to "🎮",
        Gt7Ps5DebugStateCardKey.VEHICLE_CLASS to "🏎",
        Gt7Ps5DebugStateCardKey.CURRENT_LAP to "⏱",
        Gt7Ps5DebugStateCardKey.BEST_LAP to "🏆",
        Gt7Ps5DebugStateCardKey.TYRE_TEMPERATURE to "🔥",
        Gt7Ps5DebugStateCardKey.FUEL_CONSUMPTION to "⛽",
    )

internal fun debugStateCardSymbol(cardKey: Gt7Ps5DebugStateCardKey): String = debugStateCardSymbols.getValue(cardKey)
