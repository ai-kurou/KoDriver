package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.MANUAL_READOUT_SET_ITEM_MAX
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.Res
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_category_brake
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_category_fuel
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_category_lap
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_category_tyre
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_category_virtual_energy
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_picker_remaining
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_picker_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private fun ManualReadoutItemCategory.icon(): ImageVector =
    when (this) {
        ManualReadoutItemCategory.VIRTUAL_ENERGY -> Icons.Default.Bolt
        ManualReadoutItemCategory.FUEL -> Icons.Default.LocalGasStation
        ManualReadoutItemCategory.TYRE -> Icons.Default.DonutLarge
        ManualReadoutItemCategory.BRAKE -> Icons.Default.Speed
        ManualReadoutItemCategory.LAP -> Icons.Default.Timer
    }

private fun ManualReadoutItemCategory.label(): StringResource =
    when (this) {
        ManualReadoutItemCategory.VIRTUAL_ENERGY -> Res.string.manual_readout_category_virtual_energy
        ManualReadoutItemCategory.FUEL -> Res.string.manual_readout_category_fuel
        ManualReadoutItemCategory.TYRE -> Res.string.manual_readout_category_tyre
        ManualReadoutItemCategory.BRAKE -> Res.string.manual_readout_category_brake
        ManualReadoutItemCategory.LAP -> Res.string.manual_readout_category_lap
    }

private fun manualReadoutItemCategory(itemKey: String): ManualReadoutItemCategory? =
    manualReadoutItemDefinitions.values.firstNotNullOfOrNull { it[itemKey]?.category }

// 追加候補は1セット3件上限の項目一覧のため、ImmutableList化のコストに見合わない。
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UnstableCollections")
@Composable
internal fun ManualReadoutItemPickerSheet(
    itemKeys: List<String>,
    selectedCount: Int,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        sheetMaxWidth = 560.dp,
    ) {
        ManualReadoutItemPickerSheetContent(itemKeys, selectedCount, onSelect)
    }
}

// 追加候補は1セット3件上限の項目一覧のため、ImmutableList化のコストに見合わない。
@Suppress("UnstableCollections")
@Composable
internal fun ManualReadoutItemPickerSheetContent(
    itemKeys: List<String>,
    selectedCount: Int,
    onSelect: (String) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = KoDriverSpacing.large),
    ) {
        ManualReadoutPickerHeader(selectedCount)
        val categories =
            itemKeys
                .mapNotNull { key ->
                    itemPresentations[key]?.let { presentation ->
                        manualReadoutItemCategory(key)?.let { it to (key to presentation) }
                    }
                }.groupBy({ it.first }, { it.second })
        categories.forEach { (category, items) ->
            Column(
                modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = KoDriverSpacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Icon(
                            category.icon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(KoDriverSpacing.small),
                        )
                    }
                    Text(
                        stringResource(category.label()),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        items.size.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Column {
                        items.forEachIndexed { index, (key, presentation) ->
                            if (index > 0) HorizontalDivider()
                            ListItem(
                                headlineContent = { Text(stringResource(presentation.name)) },
                                modifier =
                                    Modifier.clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        onSelect(key)
                                    },
                                colors =
                                    ListItemDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    ),
                                trailingContent = {
                                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(KoDriverSpacing.small),
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualReadoutPickerHeader(selectedCount: Int) {
    Column(
        modifier = Modifier.padding(horizontal = KoDriverSpacing.large, vertical = KoDriverSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
    ) {
        Text(
            stringResource(Res.string.manual_readout_picker_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            stringResource(Res.string.manual_readout_picker_remaining, MANUAL_READOUT_SET_ITEM_MAX - selectedCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall)) {
            repeat(MANUAL_READOUT_SET_ITEM_MAX) { index ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(KoDriverSpacing.extraSmall)
                        .background(
                            if (index < selectedCount) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            MaterialTheme.shapes.extraSmall,
                        ),
                )
            }
        }
    }
}
