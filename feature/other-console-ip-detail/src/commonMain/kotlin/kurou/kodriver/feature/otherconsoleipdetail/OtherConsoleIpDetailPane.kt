package kurou.kodriver.feature.otherconsoleipdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.otherconsoleipdetail.generated.resources.Res
import kodriver.feature.otherconsoleipdetail.generated.resources.console_ip_description
import kodriver.feature.otherconsoleipdetail.generated.resources.console_ip_invalid
import kodriver.feature.otherconsoleipdetail.generated.resources.console_ip_label
import kodriver.feature.otherconsoleipdetail.generated.resources.console_ip_placeholder
import kodriver.feature.otherconsoleipdetail.generated.resources.console_ip_save
import kodriver.feature.otherconsoleipdetail.generated.resources.console_ip_title
import kodriver.feature.otherconsoleipdetail.generated.resources.navigate_back
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OtherConsoleIpDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherConsoleIpDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherConsoleIpDetailPaneContent(
        uiState = uiState,
        onAddressChanged = viewModel::onAddressChanged,
        onSave = viewModel::onSave,
        onDismiss = viewModel::onDismiss,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
internal fun OtherConsoleIpDetailPaneContent(
    uiState: OtherConsoleIpDetailUiState,
    onAddressChanged: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onDismiss: () -> Unit = {},
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onDismiss()
            onBack()
        }
    }
    DetailPaneScaffold(
        title = stringResource(Res.string.console_ip_title),
        canNavigateBack = canNavigateBack,
        navigateBackContentDescription = stringResource(Res.string.navigate_back),
        onBack = {
            onDismiss()
            onBack()
        },
        modifier = modifier,
        navigationIconModifier = Modifier.testTag("other_detail_back"),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(Res.string.console_ip_description))
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = uiState.inputAddress,
                onValueChange = onAddressChanged,
                label = { Text(stringResource(Res.string.console_ip_label)) },
                placeholder = { Text(stringResource(Res.string.console_ip_placeholder)) },
                isError = !uiState.isInputValid,
                supportingText = if (!uiState.isInputValid) {
                    { Text(stringResource(Res.string.console_ip_invalid)) }
                } else {
                    null
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (uiState.saveFailed) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "保存に失敗しました",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSave,
                enabled = uiState.isInputValid && uiState.inputAddress.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.console_ip_save))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherConsoleIpDetailPanePreview() {
    OtherConsoleIpDetailPaneContent(
        uiState = OtherConsoleIpDetailUiState(inputAddress = "192.168.1.100"),
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherConsoleIpDetailPaneInvalidPreview() {
    OtherConsoleIpDetailPaneContent(
        uiState = OtherConsoleIpDetailUiState(inputAddress = "invalid", isInputValid = false),
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherConsoleIpDetailPaneSaveFailedPreview() {
    OtherConsoleIpDetailPaneContent(
        uiState = OtherConsoleIpDetailUiState(inputAddress = "192.168.1.100", saveFailed = true),
    )
}
