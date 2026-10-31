package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.MANUAL_READOUT_SET_ITEM_MAX
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.Res
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_add_item
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_drag_item
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_empty_hint
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_item_count
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_item_limit
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_item_limit_available
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_items_title
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_move_down
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_move_up
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_preview_play
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_preview_stop
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_remove_item
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableColumn

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
