package kurou.kodriver.feature.manualreadoutsetdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.Res
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_cancel
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_delete
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_delete_body
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_delete_confirm
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_delete_title
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_energy
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_no_items
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_set_missing
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_set_title
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
        onToggleItem = viewModel::toggleItem,
        onDelete = viewModel::deleteSet,
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
    modifier: Modifier = Modifier,
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
