package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.feature.otherserveripdetail.generated.resources.Res
import kurou.kodriver.feature.otherserveripdetail.generated.resources.navigate_back
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_connectivity_warning
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_description
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_discovering
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_discovery_show_button
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_guide_description
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_guide_link
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_invalid
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_label
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_placeholder
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_save
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_save_anyway
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val WINDOWS_INSTALL_GUIDE_URL =
    "https://github.com/ai-kurou/KoDriver/blob/main/docs/windows-install.md"

private val BUTTON_CONTENT_HEIGHT = 20.dp

/**
 * OtherServerIpDetail の画面を表示する Composable。
 */
@Composable
fun OtherServerIpDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherServerIpDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    OtherServerIpDetailPaneContent(
        uiState = uiState,
        onIpChanged = viewModel::onIpChanged,
        onSave = viewModel::onSave,
        onSaveAnyway = viewModel::onSaveAnyway,
        onDismiss = viewModel::onDismiss,
        onShowDiscoveredServers = viewModel::onShowDiscoveredServers,
        onDiscoveredServerSelected = viewModel::onDiscoveredServerSelected,
        onDiscoveryDialogConfirm = viewModel::onDiscoveryDialogConfirm,
        onDiscoveryDialogDismiss = viewModel::onDiscoveryDialogDismiss,
        onOpenGuide = { uriHandler.openUri(WINDOWS_INSTALL_GUIDE_URL) },
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * OtherServerIpDetail の画面本体を表示する Composable。
 */
@Composable
fun OtherServerIpDetailPaneContent(
    uiState: OtherServerIpDetailUiState,
    onIpChanged: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onSaveAnyway: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onShowDiscoveredServers: () -> Unit = {},
    onDiscoveredServerSelected: (DiscoveredServer) -> Unit = {},
    onDiscoveryDialogConfirm: () -> Unit = {},
    onDiscoveryDialogDismiss: () -> Unit = {},
    onOpenGuide: () -> Unit = {},
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val onOpenGuideWithHaptic: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        onOpenGuide()
    }
    val onSaveConfirmedByKeyboard: () -> Unit = {
        if (uiState.connectivityWarning) {
            onSaveAnyway()
        } else if (uiState.isInputValid && uiState.inputIp.isNotEmpty() && !uiState.isCheckingConnectivity) {
            onSave()
        }
    }
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentOnBack by rememberUpdatedState(onBack)
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            currentOnDismiss()
            currentOnBack()
        }
    }
    if (uiState.isDiscoveryDialogVisible) {
        OtherServerIpDiscoveryDialog(
            discoveredServers = uiState.discoveredServers,
            selectedDiscoveredServer = uiState.selectedDiscoveredServer,
            onServerSelected = onDiscoveredServerSelected,
            onConfirm = onDiscoveryDialogConfirm,
            onDismiss = onDiscoveryDialogDismiss,
        )
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
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(KoDriverSpacing.large),
        ) {
            Text(stringResource(Res.string.server_ip_description))
            Spacer(modifier = Modifier.height(KoDriverSpacing.medium))
            TextField(
                value = uiState.inputIp,
                onValueChange = onIpChanged,
                label = { Text(stringResource(Res.string.server_ip_label)) },
                placeholder = { Text(stringResource(Res.string.server_ip_placeholder)) },
                isError = !uiState.isInputValid,
                supportingText =
                    if (!uiState.isInputValid) {
                        { Text(stringResource(Res.string.server_ip_invalid)) }
                    } else {
                        null
                    },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSaveConfirmedByKeyboard() }),
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedVisibility(visible = uiState.connectivityWarning) {
                Column {
                    Spacer(modifier = Modifier.height(KoDriverSpacing.small))
                    Text(
                        text = stringResource(Res.string.server_ip_connectivity_warning),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Spacer(modifier = Modifier.height(KoDriverSpacing.small))
            OutlinedButton(
                onClick = onShowDiscoveredServers,
                enabled = uiState.discoveredServers.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(BUTTON_CONTENT_HEIGHT),
                ) {
                    AnimatedContent(
                        targetState = uiState.discoveredServers.isEmpty(),
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                    ) { isDiscovering ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isDiscovering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(KoDriverSpacing.small))
                                Text(stringResource(Res.string.server_ip_discovering))
                            } else {
                                Text(stringResource(Res.string.server_ip_discovery_show_button))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(KoDriverSpacing.large))
            AnimatedContent(
                targetState = uiState.connectivityWarning,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) { connectivityWarning ->
                if (connectivityWarning) {
                    Button(onClick = onSaveAnyway, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.server_ip_save_anyway))
                    }
                } else {
                    Button(
                        onClick = onSave,
                        enabled =
                            uiState.isInputValid && uiState.inputIp.isNotEmpty() && !uiState.isCheckingConnectivity,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.height(BUTTON_CONTENT_HEIGHT),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.isCheckingConnectivity) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                    )
                                    Spacer(modifier = Modifier.width(KoDriverSpacing.small))
                                }
                                Text(stringResource(Res.string.server_ip_save))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(KoDriverSpacing.extraLarge))
            Text(
                text = stringResource(Res.string.server_ip_guide_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(KoDriverSpacing.extraSmall))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onOpenGuideWithHaptic),
            ) {
                Text(
                    text = stringResource(Res.string.server_ip_guide_link),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.size(KoDriverSpacing.extraSmall))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp),
                )
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
private fun OtherServerIpDetailPaneEmptyInputPreview() {
    OtherServerIpDetailPaneContent(
        uiState = OtherServerIpDetailUiState(inputIp = ""),
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

@Preview(showBackground = true)
@Composable
private fun OtherServerIpDetailPaneDiscoveringPreview() {
    OtherServerIpDetailPaneContent(
        uiState = OtherServerIpDetailUiState(inputIp = "192.168.1.100", discoveredServers = emptyList()),
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherServerIpDetailPaneDiscoveredServersPreview() {
    OtherServerIpDetailPaneContent(
        uiState =
            OtherServerIpDetailUiState(
                inputIp = "192.168.1.100",
                discoveredServers =
                    listOf(
                        DiscoveredServer(hostName = "DESKTOP-ABC123", ipAddress = "192.168.1.10"),
                        DiscoveredServer(hostName = "DESKTOP-XYZ999", ipAddress = "192.168.1.20"),
                    ),
                isDiscoveryDialogVisible = false,
            ),
    )
}
