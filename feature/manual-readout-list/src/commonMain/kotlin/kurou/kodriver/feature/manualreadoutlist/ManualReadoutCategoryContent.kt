package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.feature.manualreadoutlist.generated.resources.Res
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_category_brake
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_category_fuel
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_category_time
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_category_tyre
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_category_virtual_energy
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_category_count
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_item_count
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_no_items
import org.jetbrains.compose.resources.stringResource

/** 読み上げカテゴリ専用。警告・温度を表す色とは別の拡張カラーを参照する。 */
@Composable
internal fun manualReadoutCategoryColor(category: ManualReadoutItemCategory): Color {
    val colors = KoDriverExtendedColors.current
    return when (category) {
        ManualReadoutItemCategory.VIRTUAL_ENERGY -> colors.categoryVirtualEnergy
        ManualReadoutItemCategory.FUEL -> colors.categoryFuel
        ManualReadoutItemCategory.TYRE -> colors.categoryTyre
        ManualReadoutItemCategory.BRAKE -> colors.categoryBrake
        ManualReadoutItemCategory.TIME -> colors.categoryTime
    }
}

internal fun manualReadoutCategoryIcon(category: ManualReadoutItemCategory): ImageVector =
    when (category) {
        ManualReadoutItemCategory.VIRTUAL_ENERGY -> Icons.Default.Bolt
        ManualReadoutItemCategory.FUEL -> Icons.Default.LocalGasStation
        ManualReadoutItemCategory.TYRE -> Icons.Default.DonutLarge
        ManualReadoutItemCategory.BRAKE -> Icons.Default.Album
        ManualReadoutItemCategory.TIME -> Icons.Default.Timer
    }

@Composable
internal fun manualReadoutCategoryLabel(category: ManualReadoutItemCategory): String =
    stringResource(
        when (category) {
            ManualReadoutItemCategory.VIRTUAL_ENERGY -> Res.string.manual_readout_category_virtual_energy
            ManualReadoutItemCategory.FUEL -> Res.string.manual_readout_category_fuel
            ManualReadoutItemCategory.TYRE -> Res.string.manual_readout_category_tyre
            ManualReadoutItemCategory.BRAKE -> Res.string.manual_readout_category_brake
            ManualReadoutItemCategory.TIME -> Res.string.manual_readout_category_time
        },
    )

@Composable
internal fun ManualReadoutCategoryChips(
    summary: ManualReadoutCategorySummary,
    itemCount: Int,
) {
    val counts = summary.counts
    if (counts.isEmpty()) {
        Text(
            if (itemCount == 0) {
                stringResource(Res.string.manual_readout_list_no_items)
            } else {
                stringResource(Res.string.manual_readout_list_item_count, itemCount)
            },
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
                            imageVector = manualReadoutCategoryIcon(category),
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
