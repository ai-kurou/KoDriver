package kurou.kodriver.feature.telemetryloglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import kodriver.feature.telemetryloglist.generated.resources.Res
import kodriver.feature.telemetryloglist.generated.resources.telemetry_log_reset_failure
import kodriver.feature.telemetryloglist.generated.resources.telemetry_log_reset_success
import kotlinx.coroutines.launch
import kurou.kodriver.core.designsystem.AppBackHandler
import kurou.kodriver.core.designsystem.predictiveBackDetailPane
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * TelemetryLog のコンテンツを表示する Composable。
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TelemetryLogContent(
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    backHandler: AppBackHandler = { _, _, _ -> },
    scrollToTopRequest: Int = 0,
    detailContent: @Composable (Long) -> Unit = {},
) {
    val viewModel = koinViewModel<TelemetryLogListViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TelemetryLogContentScaffold(
        uiState = uiState,
        onLogSelected = viewModel::selectLog,
        onClearSelectedLog = viewModel::clearSelectedLog,
        onResetClick = viewModel::onResetClick,
        onResetConfirm = viewModel::onResetConfirm,
        onResetDismiss = viewModel::onResetDismiss,
        onResetResultConsumed = viewModel::consumeResetResult,
        modifier = modifier,
        scaffoldDirective = scaffoldDirective,
        backHandler = backHandler,
        scrollToTopRequest = scrollToTopRequest,
        detailContent = detailContent,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun TelemetryLogContentScaffold(
    uiState: TelemetryLogListUiState = TelemetryLogListUiState(),
    onLogSelected: (Long) -> Unit = {},
    onClearSelectedLog: () -> Unit = {},
    onResetClick: () -> Unit = {},
    onResetConfirm: () -> Unit = {},
    onResetDismiss: () -> Unit = {},
    onResetResultConsumed: () -> Unit = {},
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
    backHandler: AppBackHandler = { _, _, _ -> },
    scrollToTopRequest: Int = 0,
    detailContent: @Composable (Long) -> Unit = {},
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>(
        scaffoldDirective = when {
            uiState.selectedLogId == null && scaffoldDirective.maxHorizontalPartitions > 1 -> {
                scaffoldDirective.copy(maxHorizontalPartitions = 1)
            }

            uiState.selectedLogId != null &&
                windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> {
                    scaffoldDirective.copy(maxHorizontalPartitions = 2)
            }

            else -> {
                scaffoldDirective
            }
        },
        initialDestinationHistory = if (uiState.selectedLogId != null) {
            listOf(
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List),
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail),
            )
        } else {
            listOf(ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List))
        },
    )
    val paneExpansionState = rememberPaneExpansionState(
        anchors = listOf(PaneExpansionAnchor.Offset.fromStart(350.dp)),
        initialAnchoredIndex = 0,
    )
    val scope = rememberCoroutineScope()
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    val navigateBack = {
        predictiveBackProgress = 0f
        scope.launch { navigator.navigateBack() }
        onClearSelectedLog()
    }

    LaunchedEffect(uiState.selectedLogId) {
        navigator.navigateTo(
            if (uiState.selectedLogId != null)
                ListDetailPaneScaffoldRole.Detail
            else
                ListDetailPaneScaffoldRole.List,
        )
    }

    backHandler(navigator.canNavigateBack(), { predictiveBackProgress = it }) { navigateBack() }

    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(Res.string.telemetry_log_reset_success)
    val failureMessage = stringResource(Res.string.telemetry_log_reset_failure)

    LaunchedEffect(uiState.resetSucceeded) {
        val resetSucceeded = uiState.resetSucceeded ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(if (resetSucceeded) successMessage else failureMessage)
        onResetResultConsumed()
    }

    if (uiState.showResetConfirmDialog) {
        TelemetryLogResetConfirmDialog(
            onConfirm = onResetConfirm,
            onDismiss = onResetDismiss,
        )
    }

    Box(modifier = modifier) {
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            scaffoldState = navigator.scaffoldState,
            paneExpansionState = paneExpansionState,
            paneExpansionDragHandle = { VerticalDivider() },
            modifier = Modifier.fillMaxSize(),
            listPane = {
                TelemetryLogListPane(
                    uiState = uiState,
                    onLogClick = onLogSelected,
                    onResetClick = onResetClick,
                    scrollToTopRequest = scrollToTopRequest,
                )
            },
            detailPane = {
                uiState.selectedLogId?.let { selectedLogId ->
                    Box(modifier = Modifier.predictiveBackDetailPane(predictiveBackProgress)) {
                        detailContent(selectedLogId)
                    }
                }
            },
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(showBackground = true)
@Composable
private fun TelemetryLogContentPreview() {
    TelemetryLogContentScaffold(
        uiState = previewTelemetryLogListUiState,
    )
}

internal val previewTelemetryLogListUiState = TelemetryLogListUiState(
    logs = listOf(
        TelemetryLog(
            id = 2,
            createdAt = 1_820_000,
            simulator = Simulator.LmuWindows,
            readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
            telemetryJson = """{"flag":"green","sector1":"clear","sector2":"clear","sector3":"clear"}""",
        ),
        TelemetryLog(
            id = 1,
            createdAt = 1_800_000,
            simulator = Simulator.Gt7Ps5,
            readoutItemKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
            telemetryJson = """{"remainingFuelLaps":3.6,"fuelPercent":18.2}""",
        ),
    ),
)
