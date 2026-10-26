package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.feature.manualreadoutlist.generated.resources.Res
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_category_brake
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_category_fuel
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_category_lap
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_category_tyre
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_category_virtual_energy
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_category_count
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_no_items
import org.jetbrains.compose.resources.stringResource

/** 読み上げカテゴリ専用。警告・温度を表す designsystem の色とは区別する。 */
@Composable
internal fun manualReadoutCategoryColor(category: ManualReadoutItemCategory): Color {
    val dark = isSystemInDarkTheme()
    return when (category) {
        ManualReadoutItemCategory.VIRTUAL_ENERGY -> if (dark) Color(0xFF6FD3EC) else Color(0xFF0A7F9C)
        ManualReadoutItemCategory.FUEL -> if (dark) Color(0xFFF0A066) else Color(0xFFC2570C)
        ManualReadoutItemCategory.TYRE -> if (dark) Color(0xFF7FD0A0) else Color(0xFF2F7D4F)
        ManualReadoutItemCategory.BRAKE -> if (dark) Color(0xFFFF9D94) else Color(0xFFB3261E)
        ManualReadoutItemCategory.LAP -> if (dark) Color(0xFFC3B2FF) else Color(0xFF6A4FC7)
    }
}

@Composable
private fun manualReadoutCategoryLabel(category: ManualReadoutItemCategory): String =
    stringResource(
        when (category) {
            ManualReadoutItemCategory.VIRTUAL_ENERGY -> Res.string.manual_readout_category_virtual_energy
            ManualReadoutItemCategory.FUEL -> Res.string.manual_readout_category_fuel
            ManualReadoutItemCategory.TYRE -> Res.string.manual_readout_category_tyre
            ManualReadoutItemCategory.BRAKE -> Res.string.manual_readout_category_brake
            ManualReadoutItemCategory.LAP -> Res.string.manual_readout_category_lap
        },
    )

@Composable
internal fun ManualReadoutCategoryChips(summary: ManualReadoutCategorySummary) {
    val counts = summary.counts
    if (counts.isEmpty()) {
        Text(
            stringResource(Res.string.manual_readout_list_no_items),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
        ) {
            counts.forEach { (category, count) ->
                val color = manualReadoutCategoryColor(category)
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Row(
                        modifier = Modifier.padding(KoDriverSpacing.extraSmall),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
                    ) {
                        Icon(
                            imageVector =
                                when (category) {
                                    ManualReadoutItemCategory.VIRTUAL_ENERGY -> Icons.Default.Bolt
                                    ManualReadoutItemCategory.FUEL -> Icons.Default.LocalGasStation
                                    ManualReadoutItemCategory.TYRE -> Icons.Default.DonutLarge
                                    ManualReadoutItemCategory.BRAKE -> Icons.Default.Speed
                                    ManualReadoutItemCategory.LAP -> Icons.Default.Timer
                                },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = color,
                        )
                        Text(
                            stringResource(
                                Res.string.manual_readout_list_category_count,
                                manualReadoutCategoryLabel(category),
                                count,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ManualReadoutCategoryDots(summary: ManualReadoutCategorySummary) {
    Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall)) {
        summary.counts.forEach { (category, count) ->
            val description =
                stringResource(
                    Res.string.manual_readout_list_category_count,
                    manualReadoutCategoryLabel(category),
                    count,
                )
            Box(
                Modifier
                    .size(8.dp)
                    .background(manualReadoutCategoryColor(category), CircleShape)
                    .semantics { contentDescription = description },
            )
        }
    }
}
