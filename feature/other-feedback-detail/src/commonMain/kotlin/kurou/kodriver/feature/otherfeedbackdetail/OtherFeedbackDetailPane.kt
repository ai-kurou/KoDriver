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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.otherfeedbackdetail.generated.resources.Res
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_description
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_diagnostics_description
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_email_invalid
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_email_label
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_email_required
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_failed
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_message_label
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_message_placeholder
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_message_required
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_name_label
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_name_required
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_send
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_sending
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_sent
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_title
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_type_bug_report
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_type_feature_request
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_type_label
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_type_other
import kodriver.feature.otherfeedbackdetail.generated.resources.feedback_type_question
import kodriver.feature.otherfeedbackdetail.generated.resources.navigate_back
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.domain.model.FeedbackType
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * OtherFeedbackDetail の画面を表示する Composable。
 */
@Composable
fun OtherFeedbackDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherFeedbackDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherFeedbackDetailPaneContent(
        uiState = uiState,
        onTypeSelected = viewModel::onTypeSelected,
        onMessageChanged = viewModel::onMessageChanged,
        onNameChanged = viewModel::onNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onSend = viewModel::onSend,
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
            Text(stringResource(Res.string.feedback_description))
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
            Spacer(modifier = Modifier.height(16.dp))
            FeedbackStatus(uiState)
            Button(
                onClick = onSend,
                enabled = uiState.canSend,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isSending) {
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
private fun FeedbackStatus(uiState: OtherFeedbackDetailUiState) {
    when {
        uiState.isSent -> {
            Text(
                text = stringResource(Res.string.feedback_sent),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        uiState.sendFailed -> {
            Text(
                text = stringResource(Res.string.feedback_failed),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        uiState.isSending -> {
            Text(
                text = stringResource(Res.string.feedback_sending),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherFeedbackDetailPanePreview() {
    OtherFeedbackDetailPaneContent(uiState = OtherFeedbackDetailUiState())
}
