package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.MANUAL_READOUT_SET_ITEM_MAX
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.Res
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_add_item
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_brake_temperature
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_brake_wear
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_category_brake
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_category_fuel
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_category_lap
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_category_tyre
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_category_virtual_energy
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_class_car_gap_ahead
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_class_car_gap_behind
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_drag_item
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_empty_hint
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_energy
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_fuel_laps
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_item_count
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_item_limit
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_item_limit_available
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_items_title
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_move_down
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_move_up
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_my_best_lap
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_picker_remaining
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_picker_title
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_preview_play
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_preview_stop
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_remaining_fuel
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_remaining_fuel_laps
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_remove_item
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_tyre_temperature
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_tyre_wear
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_tyre_wear_laps
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_virtual_energy_laps
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableColumn

/** カテゴリ・名称は画面内の表示用メタデータとして管理する。 */
internal data class ManualReadoutItemPresentation(
    val name: StringResource,
)

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

internal val itemPresentations =
    mapOf(
        LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_virtual_energy_laps,
            ),
        LmuWindowsReadoutItemKey.RemainingFuel.Laps.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_fuel_laps,
            ),
        LmuWindowsReadoutItemKey.ClassCarGap.Ahead.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_class_car_gap_ahead,
            ),
        LmuWindowsReadoutItemKey.ClassCarGap.Behind.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_class_car_gap_behind,
            ),
        LmuWindowsReadoutItemKey.PitTiming.TyreWear.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_tyre_wear_laps,
            ),
        LmuWindowsReadoutItemKey.MyBestLap.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_my_best_lap,
            ),
        AceWindowsReadoutItemKey.MyBestLap.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_my_best_lap,
            ),
        LmuWindowsReadoutItemKey.RemainingFuel.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_remaining_fuel,
            ),
        AceWindowsReadoutItemKey.RemainingFuel.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_remaining_fuel,
            ),
        AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_remaining_fuel_laps,
            ),
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_remaining_fuel,
            ),
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_remaining_fuel_laps,
            ),
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_energy,
            ),
        Gt7Ps5ReadoutItemKey.MyBestLap.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_my_best_lap,
            ),
        AceWindowsReadoutItemKey.TyreTemperature.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_tyre_temperature,
            ),
        Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_tyre_temperature,
            ),
        LmuWindowsReadoutItemKey.TyreWear.Root.value to
            ManualReadoutItemPresentation(Res.string.manual_readout_tyre_wear),
        LmuWindowsReadoutItemKey.BrakeWear.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_brake_wear,
            ),
        LmuWindowsReadoutItemKey.BrakeTemperature.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_brake_temperature,
            ),
        LmuWindowsReadoutItemKey.TyreTemperature.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_tyre_temperature,
            ),
    )

internal fun manualReadoutPreview(uiState: ManualReadoutSetDetailUiState): String =
    uiState.selectedItemKeys
        .mapNotNull { itemKey ->
            val text = uiState.itemTexts[itemKey] ?: manualReadoutItemText(uiState, itemKey)
            text.takeIf { it.isNotBlank() }?.let {
                manualReadoutItemDefinition(uiState.simulatorId, itemKey)?.previewFormatter?.invoke(it) ?: it
            }
        }.joinToString("、")

private fun manualReadoutItemText(
    uiState: ManualReadoutSetDetailUiState,
    itemKey: String,
): String = manualReadoutItemTextDefault(uiState.simulatorId, itemKey)

@Composable
internal fun ManualReadoutItemsCard(
    uiState: ManualReadoutSetDetailUiState,
    onAddItem: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onMoveItem: (Int, Int) -> Unit,
    onItemTextChanged: (String, String) -> Unit,
    onPreviewClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var showPicker by remember { mutableStateOf(false) }
    val remaining = uiState.availableItemKeys.filterNot { it in uiState.selectedItemKeys }
    val atLimit = uiState.selectedItemKeys.size >= MANUAL_READOUT_SET_ITEM_MAX
    if (showPicker) {
        ManualReadoutItemPickerSheet(
            itemKeys = remaining,
            selectedCount = uiState.selectedItemKeys.size,
            onSelect = {
                onAddItem(it)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
    DetailPaneCard(
        title = stringResource(Res.string.manual_readout_items_title),
        bottomContent = {
            Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium)) {
                if (uiState.selectedItemKeys.isEmpty()) {
                    Text(stringResource(Res.string.manual_readout_empty_hint))
                }
                ReorderableColumn(
                    list = uiState.selectedItemKeys,
                    onSettle = onMoveItem,
                    verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
                ) { index, itemKey, _ ->
                    key(itemKey) {
                        ReorderableItem {
                            Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
                                val name = itemPresentations[itemKey]?.let { stringResource(it.name) } ?: itemKey
                                val moveActions =
                                    manualReadoutMoveActions(index, uiState.selectedItemKeys.size, onMoveItem)
                                Row(
                                    Modifier.fillMaxWidth().semantics { customActions = moveActions },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Default.DragIndicator,
                                        stringResource(Res.string.manual_readout_drag_item, name),
                                        Modifier.draggableHandle().size(20.dp),
                                    )
                                    Text(name, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        onRemoveItem(itemKey)
                                    }) {
                                        Icon(
                                            Icons.Default.Close,
                                            stringResource(Res.string.manual_readout_remove_item, name),
                                        )
                                    }
                                }
                                ManualReadoutItemTextField(
                                    text = uiState.itemTexts[itemKey] ?: manualReadoutItemText(uiState, itemKey),
                                    defaultText = manualReadoutItemText(uiState, itemKey),
                                    onTextChanged = { onItemTextChanged(itemKey, it) },
                                    itemKey = itemKey,
                                    simulatorId = uiState.simulatorId,
                                )
                            }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(
                            Res.string.manual_readout_item_count,
                            uiState.selectedItemKeys.size,
                            MANUAL_READOUT_SET_ITEM_MAX,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        stringResource(
                            if (atLimit) {
                                Res.string.manual_readout_item_limit
                            } else {
                                Res.string.manual_readout_item_limit_available
                            },
                            MANUAL_READOUT_SET_ITEM_MAX,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    showPicker = true
                }, enabled = remaining.isNotEmpty() && !atLimit) {
                    Text(stringResource(Res.string.manual_readout_add_item))
                }
                if (uiState.selectedItemKeys.isNotEmpty()) {
                    val preview = manualReadoutPreview(uiState)
                    HorizontalDivider()
                    if (preview.isNotBlank()) {
                        Text(
                            preview,
                            modifier = Modifier.testTag("manual_readout_preview_sentence"),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        FilledTonalButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                onPreviewClick()
                            },
                            enabled = uiState.isTextToSpeechAvailable && preview.isNotBlank(),
                        ) {
                            Icon(
                                imageVector = if (uiState.isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                            )
                            Spacer(Modifier.width(KoDriverSpacing.small))
                            Text(
                                stringResource(
                                    if (uiState.isPreviewing) {
                                        Res.string.manual_readout_preview_stop
                                    } else {
                                        Res.string.manual_readout_preview_play
                                    },
                                ),
                            )
                        }
                    }
                }
            }
        },
    )
}

/** ドラッグできない利用者向けに、上下への移動をアクセシビリティ操作として提供する。 */
@Composable
private fun manualReadoutMoveActions(
    index: Int,
    count: Int,
    onMoveItem: (Int, Int) -> Unit,
): List<CustomAccessibilityAction> {
    val moveUp = stringResource(Res.string.manual_readout_move_up)
    val moveDown = stringResource(Res.string.manual_readout_move_down)
    return buildList {
        if (index > 0) {
            add(
                CustomAccessibilityAction(moveUp) {
                    onMoveItem(index, index - 1)
                    true
                },
            )
        }
        if (index < count - 1) {
            add(
                CustomAccessibilityAction(moveDown) {
                    onMoveItem(index, index + 1)
                    true
                },
            )
        }
    }
}

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
