package kurou.kodriver.feature.otherkeepscreenondetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.otherkeepscreenondetail.generated.resources.Res
import kodriver.feature.otherkeepscreenondetail.generated.resources.keep_screen_on_cancel
import kodriver.feature.otherkeepscreenondetail.generated.resources.keep_screen_on_description
import kodriver.feature.otherkeepscreenondetail.generated.resources.keep_screen_on_ok
import kodriver.feature.otherkeepscreenondetail.generated.resources.keep_screen_on_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OtherKeepScreenOnDetailDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherKeepScreenOnDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherKeepScreenOnDetailDialogContent(
        uiState = uiState,
        onValueChanged = viewModel::onPendingValueChanged,
        onConfirm = {
            viewModel.onConfirm()
            onDismiss()
        },
        onDismiss = {
            viewModel.onDismiss()
            onDismiss()
        },
        modifier = modifier,
    )
}

@Composable
internal fun OtherKeepScreenOnDetailDialogContent(
    uiState: OtherKeepScreenOnDetailUiState,
    onValueChanged: (Boolean) -> Unit = {},
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.keep_screen_on_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(Res.string.keep_screen_on_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                listOf(true to "ON", false to "OFF").forEach { (value, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onValueChanged(value) },
                    ) {
                        RadioButton(
                            selected = uiState.pendingKeepScreenOn == value,
                            onClick = { onValueChanged(value) },
                        )
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.keep_screen_on_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.keep_screen_on_cancel))
            }
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherKeepScreenOnDetailDialogPreview() {
    OtherKeepScreenOnDetailDialogContent(
        uiState = OtherKeepScreenOnDetailUiState(),
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherKeepScreenOnDetailDialogOnPreview() {
    OtherKeepScreenOnDetailDialogContent(
        uiState = OtherKeepScreenOnDetailUiState(keepScreenOn = true, pendingKeepScreenOn = true),
    )
}
