package kurou.kodriver.feature.otherloglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.feature.otherloglist.generated.resources.Res
import kurou.kodriver.feature.otherloglist.generated.resources.telemetry_log_delete_failure
import kurou.kodriver.feature.otherloglist.generated.resources.telemetry_log_delete_success
import kurou.kodriver.feature.otherloglist.generated.resources.telemetry_log_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * その他タブの詳細ペインにログ一覧を表示する Composable。ログをタップするとボトムシートに [detailContent] でログ詳細を表示する。
 */
@Composable
fun TelemetryLogListPaneHost(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollToTopRequest: Int = 0,
    onFeedbackClick: (Long) -> Unit = {},
    detailContent: @Composable (Long) -> Unit = {},
) {
    val viewModel = koinViewModel<TelemetryLogListViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TelemetryLogListPaneHostContent(
        uiState = uiState,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        onLogClick = viewModel::selectLog,
        onLogDetailDismiss = viewModel::clearSelectedLog,
        onDeleteClick = viewModel::onDeleteClick,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDeleteDismiss = viewModel::onDeleteDismiss,
        onDeleteResultConsumed = viewModel::consumeDeleteResult,
        modifier = modifier,
        scrollToTopRequest = scrollToTopRequest,
        onFeedbackClick = onFeedbackClick,
        detailContent = detailContent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TelemetryLogListPaneHostContent(
    uiState: TelemetryLogListUiState,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    onLogClick: (Long) -> Unit,
    onLogDetailDismiss: () -> Unit,
    onDeleteClick: (Long) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onDeleteResultConsumed: () -> Unit,
    modifier: Modifier = Modifier,
    scrollToTopRequest: Int = 0,
    onFeedbackClick: (Long) -> Unit = {},
    detailContent: @Composable (Long) -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    TelemetryLogResultSnackbarEffect(
        snackbarHostState = snackbarHostState,
        succeeded = uiState.deleteSucceeded,
        successMessage = stringResource(Res.string.telemetry_log_delete_success),
        failureMessage = stringResource(Res.string.telemetry_log_delete_failure),
        onResultConsumed = onDeleteResultConsumed,
    )

    if (uiState.pendingDeleteLogId != null) {
        TelemetryLogDeleteConfirmDialog(
            onConfirm = onDeleteConfirm,
            onDismiss = onDeleteDismiss,
        )
    }

    uiState.selectedLogId?.let { selectedLogId ->
        ModalBottomSheet(
            onDismissRequest = onLogDetailDismiss,
            sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
        ) {
            detailContent(selectedLogId)
        }
    }

    DetailPaneScaffold(
        title = stringResource(Res.string.telemetry_log_title),
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            TelemetryLogListPane(
                uiState = uiState,
                onLogClick = onLogClick,
                onFeedbackClick = onFeedbackClick,
                onDeleteClick = onDeleteClick,
                scrollToTopRequest = scrollToTopRequest,
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun TelemetryLogResultSnackbarEffect(
    snackbarHostState: SnackbarHostState,
    succeeded: Boolean?,
    successMessage: String,
    failureMessage: String,
    onResultConsumed: () -> Unit,
) {
    val currentOnResultConsumed by rememberUpdatedState(onResultConsumed)
    LaunchedEffect(succeeded) {
        val result = succeeded ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(if (result) successMessage else failureMessage)
        currentOnResultConsumed()
    }
}

@Preview(showBackground = true)
@Composable
private fun TelemetryLogListPaneHostPreview() {
    KoDriverTheme {
        TelemetryLogListPaneHostContent(
            uiState = previewTelemetryLogListUiState,
            canNavigateBack = false,
            onBack = {},
            onLogClick = {},
            onLogDetailDismiss = {},
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteDismiss = {},
            onDeleteResultConsumed = {},
        )
    }
}
