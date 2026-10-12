package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.core.designsystem.KoDriverSpacing

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
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large, color = colors.surfaceContainerLow) {
        Column(
            modifier =
                Modifier
                    .drawBehind {
                        drawRect(accent, size = Size(4.dp.toPx(), size.height))
                    }.padding(KoDriverSpacing.large),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
            ) {
                Box(
                    modifier = Modifier.size(28.dp).background(accent.copy(alpha = 0.14f), MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center,
                ) {
                    // タイトルと重複する装飾のため、スクリーンリーダーの読み上げ対象から外す。
                    Text(
                        text = debugStateCardSymbol(cardKey),
                        modifier = Modifier.clearAndSetSemantics {},
                        color = accent,
                    )
                }
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                )
                Canvas(Modifier.size(16.dp, 24.dp)) {
                    repeat(2) { column ->
                        repeat(3) { row ->
                            drawCircle(
                                colors.onSurfaceVariant.copy(alpha = 0.35f),
                                radius = 1.5.dp.toPx(),
                                center = Offset(size.width * (column + 1) / 3, size.height * (row + 1) / 4),
                            )
                        }
                    }
                }
            }
            content()
        }
    }
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
