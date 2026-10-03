package kurou.kodriver.feature.debugstatedetail

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
import kurou.kodriver.domain.model.DebugStateCardKey

@Composable
internal fun DebugStateCardFrame(
    cardKey: DebugStateCardKey,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val accent =
        when (cardKey) {
            DebugStateCardKey.SIMULATOR, DebugStateCardKey.VEHICLE_CLASS,
            DebugStateCardKey.VEHICLE_LOCATION,
            -> {
                colors.primary
            }

            DebugStateCardKey.TYRE_TEMPERATURE, DebugStateCardKey.TYRE_CARCASS_TEMPERATURE,
            DebugStateCardKey.BRAKE_TEMPERATURE, DebugStateCardKey.TYRE_WEAR,
            DebugStateCardKey.VEHICLE_DAMAGE,
            -> {
                colors.error
            }

            DebugStateCardKey.FUEL_CONSUMPTION, DebugStateCardKey.PIT_TIMING_REMAINING_LAPS -> {
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
        DebugStateCardKey.SIMULATOR to "🎮",
        DebugStateCardKey.VEHICLE_CLASS to "🏎",
        DebugStateCardKey.VEHICLE_LOCATION to "⌖",
        DebugStateCardKey.FLAG_INFO to "⚑",
        DebugStateCardKey.GAME_PHASE to "🚦",
        DebugStateCardKey.SESSION to "◷",
        DebugStateCardKey.YELLOW_FLAG_STATE to "⚠",
        DebugStateCardKey.CURRENT_LAP to "⏱",
        DebugStateCardKey.SIDE_BY_SIDE_VEHICLES to "↔",
        DebugStateCardKey.BEST_LAP to "🏆",
        DebugStateCardKey.TYRE_TEMPERATURE to "🔥",
        DebugStateCardKey.TYRE_CARCASS_TEMPERATURE to "◎",
        DebugStateCardKey.BRAKE_TEMPERATURE to "◉",
        DebugStateCardKey.TYRE_WEAR to "◴",
        DebugStateCardKey.FUEL_CONSUMPTION to "⛽",
        DebugStateCardKey.PIT_TIMING_REMAINING_LAPS to "⚒",
        DebugStateCardKey.VEHICLE_DAMAGE to "💥",
    )

internal fun debugStateCardSymbol(cardKey: DebugStateCardKey): String = debugStateCardSymbols.getValue(cardKey)
