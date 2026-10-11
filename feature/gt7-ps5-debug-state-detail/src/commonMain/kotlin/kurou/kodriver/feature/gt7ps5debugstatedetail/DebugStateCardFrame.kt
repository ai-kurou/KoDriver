package kurou.kodriver.feature.gt7ps5debugstatedetail

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
            DebugStateCardKey.SIMULATOR, DebugStateCardKey.VEHICLE_CLASS -> colors.primary
            DebugStateCardKey.TYRE_TEMPERATURE -> colors.error
            DebugStateCardKey.FUEL_CONSUMPTION -> KoDriverExtendedColors.current.onWarningContainer
            else -> colors.tertiary
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
            }
            content()
        }
    }
}

private val debugStateCardSymbols =
    mapOf(
        DebugStateCardKey.SIMULATOR to "🎮",
        DebugStateCardKey.VEHICLE_CLASS to "🏎",
        DebugStateCardKey.CURRENT_LAP to "⏱",
        DebugStateCardKey.BEST_LAP to "🏆",
        DebugStateCardKey.TYRE_TEMPERATURE to "🔥",
        DebugStateCardKey.FUEL_CONSUMPTION to "⛽",
    )

internal fun debugStateCardSymbol(cardKey: DebugStateCardKey): String = debugStateCardSymbols.getValue(cardKey)
