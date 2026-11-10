package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.DebugStateCardAccent
import kurou.kodriver.core.designsystem.DebugStateCardStyle
import kurou.kodriver.core.designsystem.DebugStateCardFrame as CommonDebugStateCardFrame

@Composable
internal fun DebugStateCardFrame(
    cardKey: Gt7Ps5DebugStateCardKey,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    CommonDebugStateCardFrame(
        title = title,
        style = debugStateCardStyles.getValue(cardKey),
        modifier = modifier,
        showHandle = false,
        content = content,
    )
}

private val debugStateCardStyles =
    mapOf(
        Gt7Ps5DebugStateCardKey.SIMULATOR to DebugStateCardStyle("🎮", DebugStateCardAccent.PRIMARY),
        Gt7Ps5DebugStateCardKey.VEHICLE_CLASS to DebugStateCardStyle("🏎", DebugStateCardAccent.PRIMARY),
        Gt7Ps5DebugStateCardKey.CURRENT_LAP to DebugStateCardStyle("⏱", DebugStateCardAccent.TERTIARY),
        Gt7Ps5DebugStateCardKey.BEST_LAP to DebugStateCardStyle("🏆", DebugStateCardAccent.TERTIARY),
        Gt7Ps5DebugStateCardKey.TYRE_TEMPERATURE to DebugStateCardStyle("🔥", DebugStateCardAccent.ERROR),
        Gt7Ps5DebugStateCardKey.FUEL_CONSUMPTION to DebugStateCardStyle("⛽", DebugStateCardAccent.WARNING),
    )

internal fun debugStateCardSymbol(cardKey: Gt7Ps5DebugStateCardKey): String =
    debugStateCardStyles.getValue(cardKey).symbol
