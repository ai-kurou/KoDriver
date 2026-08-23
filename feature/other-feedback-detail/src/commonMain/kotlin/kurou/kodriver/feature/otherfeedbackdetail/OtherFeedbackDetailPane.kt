package kurou.kodriver.feature.otherfeedbackdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.domain.model.FeedbackType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.Res
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_attached_telemetry_log_chip
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_description_prefix
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_description_sentry_link
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_description_suffix
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_detach_telemetry_log
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_diagnostics_description
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_email_invalid
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_email_label
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_email_required
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_failed
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_message_label
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_message_placeholder
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_message_required
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_name_label
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_name_required
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_send
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_sending
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_sent
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_title
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_type_bug_report
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_type_feature_request
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_type_label
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_type_other
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.feedback_type_question
import kurou.kodriver.feature.otherfeedbackdetail.generated.resources.navigate_back
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val SENTRY_URL = "https://sentry.io/"
private const val SENTRY_LINK_ICON_ID = "sentry-link-icon"

/**
 * OtherFeedbackDetail の画面を表示する Composable。
 */
@Composable
fun OtherFeedbackDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    telemetryLogId: Long? = null,
    telemetryLogAttachRequestId: Long = 0,
) {
    val viewModel: OtherFeedbackDetailViewModel = koinViewModel()
    LaunchedEffect(telemetryLogId, telemetryLogAttachRequestId) {
        viewModel.setTelemetryLogId(telemetryLogId)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    OtherFeedbackDetailPaneContent(
        uiState = uiState,
        onTypeSelected = viewModel::onTypeSelected,
        onMessageChanged = viewModel::onMessageChanged,
        onNameChanged = viewModel::onNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onSend = viewModel::onSend,
        onDetachTelemetryLog = viewModel::onDetachTelemetryLog,
        onOpenSentry = { uriHandler.openUri(SENTRY_URL) },
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * OtherFeedbackDetail の画面本体を表示する Composable。
 */
@Composable
fun OtherFeedbackDetailPaneContent(
    uiState: OtherFeedbackDetailUiState,
    onTypeSelected: (FeedbackType) -> Unit = {},
    onMessageChanged: (String) -> Unit = {},
    onNameChanged: (String) -> Unit = {},
    onEmailChanged: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onDetachTelemetryLog: () -> Unit = {},
    onOpenSentry: () -> Unit = {},
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    DetailPaneScaffold(
        title = stringResource(Res.string.feedback_title),
        canNavigateBack = canNavigateBack,
        navigateBackContentDescription = stringResource(Res.string.navigate_back),
        onBack = onBack,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            Text(
                text =
                    buildAnnotatedString {
                        append(stringResource(Res.string.feedback_description_prefix))
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = "sentry",
                                styles =
                                    TextLinkStyles(
                                        style =
                                            SpanStyle(
                                                color = MaterialTheme.colorScheme.primary,
                                                textDecoration = TextDecoration.Underline,
                                            ),
                                    ),
                                linkInteractionListener = { onOpenSentry() },
                            ),
                        ) {
                            append(stringResource(Res.string.feedback_description_sentry_link))
                            appendInlineContent(SENTRY_LINK_ICON_ID)
                        }
                        append(stringResource(Res.string.feedback_description_suffix))
                    },
                inlineContent =
                    mapOf(
                        SENTRY_LINK_ICON_ID to
                            InlineTextContent(
                                placeholder =
                                    Placeholder(
                                        width = 12.sp,
                                        height = 12.sp,
                                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                                    ),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp),
                                )
                            },
                    ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.feedback_type_label),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            FeedbackTypeOption(
                type = FeedbackType.BugReport,
                selectedType = uiState.type,
                label = stringResource(Res.string.feedback_type_bug_report),
                onTypeSelected = onTypeSelected,
            )
            FeedbackTypeOption(
                type = FeedbackType.FeatureRequest,
                selectedType = uiState.type,
                label = stringResource(Res.string.feedback_type_feature_request),
                onTypeSelected = onTypeSelected,
            )
            FeedbackTypeOption(
                type = FeedbackType.Question,
                selectedType = uiState.type,
                label = stringResource(Res.string.feedback_type_question),
                onTypeSelected = onTypeSelected,
            )
            FeedbackTypeOption(
                type = FeedbackType.Other,
                selectedType = uiState.type,
                label = stringResource(Res.string.feedback_type_other),
                onTypeSelected = onTypeSelected,
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = uiState.message,
                onValueChange = onMessageChanged,
                label = { Text(stringResource(Res.string.feedback_message_label)) },
                placeholder = { Text(stringResource(Res.string.feedback_message_placeholder)) },
                isError = uiState.showMessageError,
                supportingText =
                    if (uiState.showMessageError) {
                        { Text(stringResource(Res.string.feedback_message_required)) }
                    } else {
                        null
                    },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = uiState.name,
                onValueChange = onNameChanged,
                label = { Text(stringResource(Res.string.feedback_name_label)) },
                isError = uiState.showNameError,
                supportingText =
                    if (uiState.showNameError) {
                        { Text(stringResource(Res.string.feedback_name_required)) }
                    } else {
                        null
                    },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                label = { Text(stringResource(Res.string.feedback_email_label)) },
                isError = uiState.showEmailError,
                supportingText =
                    if (uiState.showEmailError) {
                        val messageRes =
                            if (uiState.showEmailFormatError) {
                                Res.string.feedback_email_invalid
                            } else {
                                Res.string.feedback_email_required
                            }
                        { Text(stringResource(messageRes)) }
                    } else {
                        null
                    },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.feedback_diagnostics_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.attachedTelemetryLog != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AttachedTelemetryLogChip(
                    telemetryLog = uiState.attachedTelemetryLog,
                    onDetach = onDetachTelemetryLog,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            FeedbackStatus(uiState)
            Button(
                onClick = onSend,
                enabled = uiState.canSend,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.sendStatus == FeedbackSendStatus.Sending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(stringResource(Res.string.feedback_send))
                }
            }
        }
    }
}

@Composable
private fun FeedbackTypeOption(
    type: FeedbackType,
    selectedType: FeedbackType,
    label: String,
    onTypeSelected: (FeedbackType) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onTypeSelected(type) },
    ) {
        RadioButton(
            selected = type == selectedType,
            onClick = { onTypeSelected(type) },
        )
        Text(label)
    }
}

@Composable
private fun AttachedTelemetryLogChip(
    telemetryLog: TelemetryLog,
    onDetach: () -> Unit,
) {
    InputChip(
        selected = false,
        onClick = {},
        label = {
            Text(stringResource(Res.string.feedback_attached_telemetry_log_chip, telemetryLog.id.toString()))
        },
        trailingIcon = {
            IconButton(
                onClick = onDetach,
                modifier = Modifier.size(InputChipDefaults.IconSize),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.feedback_detach_telemetry_log),
                    modifier = Modifier.size(InputChipDefaults.IconSize),
                )
            }
        },
    )
}

@Composable
private fun FeedbackStatus(uiState: OtherFeedbackDetailUiState) {
    when (uiState.sendStatus) {
        FeedbackSendStatus.Sent -> {
            Text(
                text = stringResource(Res.string.feedback_sent),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        FeedbackSendStatus.Failed -> {
            Text(
                text = stringResource(Res.string.feedback_failed),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        FeedbackSendStatus.Sending -> {
            Text(
                text = stringResource(Res.string.feedback_sending),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        FeedbackSendStatus.Idle -> {}
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherFeedbackDetailPanePreview() {
    OtherFeedbackDetailPaneContent(uiState = OtherFeedbackDetailUiState())
}

@Preview(showBackground = true)
@Composable
private fun OtherFeedbackDetailPaneAttachedTelemetryLogPreview() {
    OtherFeedbackDetailPaneContent(
        uiState =
            OtherFeedbackDetailUiState(
                attachedTelemetryLog =
                    TelemetryLog(
                        id = 1L,
                        createdAt = 0L,
                        simulator = Simulator.LmuWindows,
                        readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                        telemetryJson = "",
                    ),
            ),
    )
}
