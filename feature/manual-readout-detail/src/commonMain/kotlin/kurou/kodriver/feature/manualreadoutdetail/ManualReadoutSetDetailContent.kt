package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneLabeledTextField
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverDestructiveConfirmDialog
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.rememberPendingText
import kurou.kodriver.core.designsystem.rememberTextInsertState
import kurou.kodriver.domain.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.findUnknownLmuWindowsRemainingVirtualEnergyReadoutPlaceholders
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.Res
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_cancel
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_delete
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_delete_body
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_delete_confirm
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_delete_title
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_no_items
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_percent_hint
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_percent_insert
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_set_missing
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_set_title
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_text_blank
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_text_label
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_text_reset
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_text_unknown
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ManualReadoutSetDetailContent(
    simulatorId: String,
    setId: String,
    onDeleted: () -> Unit,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showHotkey: Boolean = isManualReadoutHotkeySupported(),
) {
    val viewModel: ManualReadoutSetDetailViewModel =
        koinViewModel(key = "$simulatorId:$setId", parameters = { parametersOf(simulatorId, setId) })
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) {
        onDispose { viewModel.onPreviewStopped() }
    }
    val currentOnDeleted by rememberUpdatedState(onDeleted)
    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) currentOnDeleted()
    }
    ManualReadoutSetDetailPane(
        uiState = uiState,
        onAssignHotkey = viewModel::assignHotkey,
        onSelectModifiers = viewModel::selectHotkeyModifiers,
        onClearHotkey = viewModel::clearHotkey,
        onRetryHotkeyRegistration = viewModel::onRetryHotkeyRegistration,
        showHotkey = showHotkey,
        onAddItem = viewModel::addItem,
        onRemoveItem = viewModel::removeItem,
        onMoveItem = viewModel::moveItem,
        onDelete = viewModel::deleteSet,
        onItemTextChanged = viewModel::updateItemText,
        onPreviewClick = viewModel::onPreviewClicked,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
internal fun ManualReadoutSetDetailPane(
    uiState: ManualReadoutSetDetailUiState,
    onAddItem: (String) -> Unit,
    onDelete: () -> Unit,
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    onRemoveItem: (String) -> Unit = {},
    onMoveItem: (Int, Int) -> Unit = { _, _ -> },
    onItemTextChanged: (String, String) -> Unit = { _, _ -> },
    onPreviewClick: () -> Unit = {},
    onAssignHotkey: (Int) -> Unit = {},
    onSelectModifiers: (ManualReadoutHotkeyModifiers) -> Unit = {},
    onClearHotkey: () -> Unit = {},
    onRetryHotkeyRegistration: () -> Unit = {},
    modifier: Modifier = Modifier,
    showHotkey: Boolean = isManualReadoutHotkeySupported(),
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ManualReadoutSetDeleteConfirmDialog(
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
    val number = uiState.setNumber
    DetailPaneScaffold(
        title =
            if (number != null) {
                stringResource(Res.string.manual_readout_set_title, number)
            } else {
                stringResource(Res.string.manual_readout_title)
            },
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
        actions = {
            if (number != null) {
                val haptic = LocalHapticFeedback.current
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        showDeleteDialog = true
                    },
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(Res.string.manual_readout_delete))
                }
            }
        },
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(KoDriverSpacing.large),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
        ) {
            if (number != null) {
                if (uiState.availableItemKeys.isEmpty()) {
                    Text(stringResource(Res.string.manual_readout_no_items))
                }
                if (uiState.availableItemKeys.isNotEmpty()) {
                    ManualReadoutItemsCard(
                        uiState,
                        onAddItem,
                        onRemoveItem,
                        onMoveItem,
                        onItemTextChanged,
                        onPreviewClick,
                    )
                }
                if (showHotkey) {
                    ManualReadoutHotkeyCard(
                        uiState,
                        onAssignHotkey,
                        onClearHotkey,
                        onRetryHotkeyRegistration,
                        onSelectModifiers,
                    )
                }
            } else if (!uiState.isLoading) {
                Text(stringResource(Res.string.manual_readout_set_missing))
            }
        }
    }
}

@Composable
internal fun ManualReadoutSetDeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    KoDriverDestructiveConfirmDialog(
        title = stringResource(Res.string.manual_readout_delete_title),
        body = stringResource(Res.string.manual_readout_delete_body),
        confirmLabel = stringResource(Res.string.manual_readout_delete_confirm),
        dismissLabel = stringResource(Res.string.manual_readout_cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

/** 選択中の項目の文言を編集し、入力ごとに保存する。 */
@Composable
internal fun ManualReadoutItemTextField(
    text: String,
    defaultText: String,
    onTextChanged: (String) -> Unit,
) {
    val textState = rememberPendingText(text, READOUT_CUSTOM_TEXT_MAX_LENGTH)
    val currentText = textState.currentText
    val insertState = rememberTextInsertState()
    val changeText: (String) -> Unit = {
        textState.change(it)
        onTextChanged(it)
    }
    val unknown = findUnknownLmuWindowsRemainingVirtualEnergyReadoutPlaceholders(currentText).joinToString("、")
    Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
        DetailPaneLabeledTextField(
            label = stringResource(Res.string.manual_readout_text_label),
            value = currentText,
            insertState = insertState,
            defaultValue = defaultText,
            maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
            onValueChangeFinished = changeText,
            onPreviewClick = null,
            onResetToDefault = { changeText(defaultText) },
            resetContentDescription = stringResource(Res.string.manual_readout_text_reset),
            supportingText =
                when {
                    unknown.isNotEmpty() -> stringResource(Res.string.manual_readout_text_unknown, unknown)
                    currentText.isBlank() -> stringResource(Res.string.manual_readout_text_blank)
                    else -> null
                },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
        ) {
            AssistChip(
                onClick = { insertState.insert(LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER) },
                label = { Text(stringResource(Res.string.manual_readout_percent_insert)) },
                // 押下で入力欄のフォーカスが外れると選択範囲が解除されるため、チップはフォーカスを取らない。
                modifier = Modifier.focusProperties { canFocus = false },
            )
            Text(
                text = stringResource(Res.string.manual_readout_percent_hint),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
