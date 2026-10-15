package kurou.kodriver.feature.manualreadoutsetdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneLabeledTextField
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.rememberPendingText
import kurou.kodriver.domain.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.findUnknownLmuWindowsRemainingVirtualEnergyReadoutPlaceholders
import kurou.kodriver.domain.model.manualReadoutItemTextDefault
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.Res
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_cancel
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_delete
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_delete_body
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_delete_confirm
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_delete_title
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_energy
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_no_items
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_percent_hint
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_percent_insert
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_set_missing
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_set_title
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_text_blank
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_text_label
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_text_reset
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_text_unknown
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_title
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.navigate_back
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
    val currentOnDeleted by rememberUpdatedState(onDeleted)
    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) currentOnDeleted()
    }
    ManualReadoutSetDetailPane(
        uiState = uiState,
        onAssignHotkey = viewModel::assignHotkey,
        onClearHotkey = viewModel::clearHotkey,
        onRetryHotkeyRegistration = viewModel::onRetryHotkeyRegistration,
        showHotkey = showHotkey,
        onToggleItem = viewModel::toggleItem,
        onDelete = viewModel::deleteSet,
        onItemTextChanged = viewModel::updateItemText,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
internal fun ManualReadoutSetDetailPane(
    uiState: ManualReadoutSetDetailUiState,
    onToggleItem: (String) -> Unit,
    onDelete: () -> Unit,
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    onItemTextChanged: (String, String) -> Unit = { _, _ -> },
    onAssignHotkey: (Int) -> Unit = {},
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
        navigateBackContentDescription = stringResource(Res.string.navigate_back),
        onBack = onBack,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(KoDriverSpacing.large),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
        ) {
            if (number != null) {
                if (uiState.availableItemKeys.isEmpty()) {
                    Text(stringResource(Res.string.manual_readout_no_items))
                }
                uiState.availableItemKeys.forEach { itemKey ->
                    val itemName = stringResource(Res.string.manual_readout_energy)
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(itemName, modifier = Modifier.weight(1f))
                        Switch(
                            checked = itemKey in uiState.selectedItemKeys,
                            onCheckedChange = { onToggleItem(itemKey) },
                            modifier = Modifier.semantics { contentDescription = itemName },
                        )
                    }
                    if (itemKey in uiState.selectedItemKeys) {
                        ManualReadoutItemTextField(
                            text = uiState.itemTexts[itemKey] ?: manualReadoutItemTextDefault(itemKey),
                            defaultText = manualReadoutItemTextDefault(itemKey),
                            onTextChanged = { onItemTextChanged(itemKey, it) },
                        )
                    }
                }
                if (showHotkey) {
                    ManualReadoutHotkeyCard(uiState, onAssignHotkey, onClearHotkey, onRetryHotkeyRegistration)
                }
                TextButton(onClick = { showDeleteDialog = true }) {
                    Text(stringResource(Res.string.manual_readout_delete), color = MaterialTheme.colorScheme.error)
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.manual_readout_delete_title)) },
        text = { Text(stringResource(Res.string.manual_readout_delete_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.manual_readout_delete_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.manual_readout_cancel)) }
        },
    )
}

/** 選択中の項目の文言を編集し、入力ごとに保存する。 */
@Composable
private fun ManualReadoutItemTextField(
    text: String,
    defaultText: String,
    onTextChanged: (String) -> Unit,
) {
    val textState = rememberPendingText(text, READOUT_CUSTOM_TEXT_MAX_LENGTH)
    val currentText = textState.currentText
    val changeText: (String) -> Unit = {
        textState.change(it)
        onTextChanged(it)
    }
    val unknown = findUnknownLmuWindowsRemainingVirtualEnergyReadoutPlaceholders(currentText).joinToString("、")
    Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
        DetailPaneLabeledTextField(
            label = stringResource(Res.string.manual_readout_text_label),
            value = currentText,
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
                onClick = { changeText(currentText + LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER) },
                label = { Text(stringResource(Res.string.manual_readout_percent_insert)) },
                enabled =
                    currentText.length + LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER.length <=
                        READOUT_CUSTOM_TEXT_MAX_LENGTH,
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
