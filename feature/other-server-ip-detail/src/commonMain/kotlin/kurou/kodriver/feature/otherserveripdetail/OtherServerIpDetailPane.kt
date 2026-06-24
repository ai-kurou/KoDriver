package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.otherserveripdetail.generated.resources.Res
import kodriver.feature.otherserveripdetail.generated.resources.navigate_back
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_connectivity_warning
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_description
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_invalid
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_label
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_placeholder
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_save
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_save_anyway
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_title
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OtherServerIpDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherServerIpDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherServerIpDetailPaneContent(
        uiState = uiState,
        onIpChanged = viewModel::onIpChanged,
        onSave = viewModel::onSave,
        onSaveAnyway = viewModel::onSaveAnyway,
        onDismiss = viewModel::onDismiss,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
internal fun OtherServerIpDetailPaneContent(
    uiState: OtherServerIpDetailUiState,
    onIpChanged: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onSaveAnyway: () -> Unit = {},
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
        title = stringResource(Res.string.server_ip_title),
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
            Text(stringResource(Res.string.server_ip_description))
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = uiState.inputIp,
                onValueChange = onIpChanged,
                label = { Text(stringResource(Res.string.server_ip_label)) },
                placeholder = { Text(stringResource(Res.string.server_ip_placeholder)) },
                isError = !uiState.isInputValid,
                supportingText = if (!uiState.isInputValid) {
                    { Text(stringResource(Res.string.server_ip_invalid)) }
                } else {
                    null
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (uiState.connectivityWarning) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.server_ip_connectivity_warning),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.connectivityWarning) {
                Button(onClick = onSaveAnyway, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.server_ip_save_anyway))
                }
            } else {
                Button(
                    onClick = onSave,
                    enabled = uiState.isInputValid && uiState.inputIp.isNotEmpty() && !uiState.isCheckingConnectivity,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.isCheckingConnectivity) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(stringResource(Res.string.server_ip_save))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherServerIpDetailPanePreview() {
    OtherServerIpDetailPaneContent(
        uiState = OtherServerIpDetailUiState(inputIp = "192.168.1.100"),
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherServerIpDetailPaneInvalidPreview() {
    OtherServerIpDetailPaneContent(
        uiState = OtherServerIpDetailUiState(inputIp = "invalid", isInputValid = false),
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherServerIpDetailPaneConnectivityWarningPreview() {
    OtherServerIpDetailPaneContent(
        uiState = OtherServerIpDetailUiState(inputIp = "192.168.1.100", connectivityWarning = true),
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherServerIpDetailPaneCheckingConnectivityPreview() {
    OtherServerIpDetailPaneContent(
        uiState = OtherServerIpDetailUiState(inputIp = "192.168.1.100", isCheckingConnectivity = true),
    )
}
