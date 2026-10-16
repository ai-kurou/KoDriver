package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.MANUAL_READOUT_SET_ITEM_MAX
import kurou.kodriver.domain.model.formatLmuWindowsBrakeWearReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsRemainingVirtualEnergyReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsTyreWearReadoutText
import kurou.kodriver.domain.model.manualReadoutItemTextDefault
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.Res
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_add_item
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_brake_wear
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_cancel
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_category_brake
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_category_energy
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_category_tyre
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_drag_item
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_empty_hint
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_energy
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_item_limit
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_items_title
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_move_down
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_move_up
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_preview_play
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_preview_stop
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_remove_item
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_tyre_wear
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableColumn

/** カテゴリ・名称は画面内の表示用メタデータとして管理する。 */
private data class ManualReadoutItemPresentation(
    val name: StringResource,
    val category: StringResource,
)

private val itemPresentations =
    mapOf(
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value to
            ManualReadoutItemPresentation(Res.string.manual_readout_energy, Res.string.manual_readout_category_energy),
        LmuWindowsReadoutItemKey.TyreWear.Root.value to
            ManualReadoutItemPresentation(Res.string.manual_readout_tyre_wear, Res.string.manual_readout_category_tyre),
        LmuWindowsReadoutItemKey.BrakeWear.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_brake_wear,
                Res.string.manual_readout_category_brake,
            ),
    )

private const val PREVIEW_BRAKE_WEAR_PERCENT = 50
private const val PREVIEW_TYRE_WEAR_PERCENT = 50
private const val PREVIEW_ENERGY_PERCENT = 50

internal fun manualReadoutPreview(uiState: ManualReadoutSetDetailUiState): String =
    uiState.selectedItemKeys
        .mapNotNull { itemKey ->
            val text = uiState.itemTexts[itemKey] ?: manualReadoutItemTextDefault(itemKey)
            text.takeIf { it.isNotBlank() }?.let {
                when (itemKey) {
                    LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value -> {
                        formatLmuWindowsRemainingVirtualEnergyReadoutText(it, PREVIEW_ENERGY_PERCENT)
                    }

                    LmuWindowsReadoutItemKey.BrakeWear.Root.value -> {
                        formatLmuWindowsBrakeWearReadoutText(it, PREVIEW_BRAKE_WEAR_PERCENT)
                    }

                    LmuWindowsReadoutItemKey.TyreWear.Root.value -> {
                        formatLmuWindowsTyreWearReadoutText(it, PREVIEW_TYRE_WEAR_PERCENT)
                    }

                    else -> {
                        it
                    }
                }
            }
        }.joinToString("、")

@Composable
internal fun ManualReadoutItemsCard(
    uiState: ManualReadoutSetDetailUiState,
    onAddItem: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onMoveItem: (Int, Int) -> Unit,
    onItemTextChanged: (String, String) -> Unit,
    onPreviewClick: () -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val remaining = uiState.availableItemKeys.filterNot { it in uiState.selectedItemKeys }
    val atLimit = uiState.selectedItemKeys.size >= MANUAL_READOUT_SET_ITEM_MAX
    if (showPicker) {
        ManualReadoutItemPickerDialog(
            itemKeys = remaining,
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
                                    IconButton(onClick = { onRemoveItem(itemKey) }) {
                                        Icon(
                                            Icons.Default.Close,
                                            stringResource(Res.string.manual_readout_remove_item, name),
                                        )
                                    }
                                }
                                ManualReadoutItemTextField(
                                    text = uiState.itemTexts[itemKey] ?: manualReadoutItemTextDefault(itemKey),
                                    defaultText = manualReadoutItemTextDefault(itemKey),
                                    onTextChanged = { onItemTextChanged(itemKey, it) },
                                )
                            }
                        }
                    }
                }
                TextButton(onClick = { showPicker = true }, enabled = remaining.isNotEmpty() && !atLimit) {
                    Text(stringResource(Res.string.manual_readout_add_item))
                }
                if (atLimit) {
                    Text(stringResource(Res.string.manual_readout_item_limit, MANUAL_READOUT_SET_ITEM_MAX))
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
                            onClick = onPreviewClick,
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
@Suppress("UnstableCollections")
@Composable
internal fun ManualReadoutItemPickerDialog(
    itemKeys: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.manual_readout_add_item)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                val categories =
                    itemKeys
                        .mapNotNull { key -> itemPresentations[key]?.let { key to it } }
                        .groupBy { it.second.category }
                categories.forEach { (category, items) ->
                    Text(stringResource(category), style = MaterialTheme.typography.titleSmall)
                    items.forEach { (key, presentation) ->
                        TextButton(onClick = { onSelect(key) }) { Text(stringResource(presentation.name)) }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.manual_readout_cancel)) }
        },
    )
}
