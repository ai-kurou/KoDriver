package kurou.kodriver.feature.readoutlist

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.launch
import kurou.kodriver.core.designsystem.AppBackHandler
import kurou.kodriver.core.designsystem.predictiveBackDetailPane
import kurou.kodriver.core.designsystem.readoutItemDisplayName
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.koin.compose.viewmodel.koinViewModel

/**
 * Readout のコンテンツを表示する Composable。
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ReadoutContent(
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    backHandler: AppBackHandler = { _, _, _ -> },
    scrollToTopRequest: Int = 0,
    detailContent: @Composable (ReadoutListItemType) -> Unit = {},
) {
    val viewModel: ReadoutListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateInLifecycle()
    ReadoutContent(
        uiState = uiState,
        onSimulatorSelected = viewModel::onSimulatorSelected,
        onMove = viewModel::moveItem,
        onReadoutEnabledChanged = viewModel::onReadoutEnabledChanged,
        onQueueEnabledChanged = viewModel::onQueueEnabledChanged,
        onItemSelected = viewModel::onItemSelected,
        onClearSelectedItem = viewModel::clearSelectedItem,
        modifier = modifier,
        scaffoldDirective = scaffoldDirective,
        backHandler = backHandler,
        scrollToTopRequest = scrollToTopRequest,
        detailContent = detailContent,
    )
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun ReadoutContent(
    uiState: ReadoutListUiState,
    onSimulatorSelected: (Simulator) -> Unit,
    onMove: (Int, Int) -> Unit,
    onReadoutEnabledChanged: (ReadoutItemKey, Boolean) -> Unit,
    onQueueEnabledChanged: (ReadoutItemKey, Boolean) -> Unit,
    onItemSelected: (ReadoutItemKey) -> Unit,
    onClearSelectedItem: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
    backHandler: AppBackHandler = { _, _, _ -> },
    scrollToTopRequest: Int = 0,
    detailContent: @Composable (ReadoutListItemType) -> Unit = {},
) {
    val navigator =
        rememberListDetailPaneScaffoldNavigator<Nothing>(
            scaffoldDirective =
                when {
                    uiState.selectedItem == null && scaffoldDirective.maxHorizontalPartitions > 1 -> {
                        scaffoldDirective.copy(maxHorizontalPartitions = 1)
                    }

                    uiState.selectedItem != null &&
                        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> {
                        scaffoldDirective.copy(maxHorizontalPartitions = 2)
                    }

                    else -> {
                        scaffoldDirective
                    }
                },
            initialDestinationHistory =
                if (uiState.selectedItem != null) {
                    listOf(
                        ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List),
                        ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail),
                    )
                } else {
                    listOf(ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List))
                },
        )
    val scope = rememberCoroutineScope()
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    val navigateBack = {
        predictiveBackProgress = 0f
        scope.launch { navigator.navigateBack() }
        onClearSelectedItem()
    }
    val paneExpansionState =
        rememberPaneExpansionState(
            anchors = listOf(PaneExpansionAnchor.Offset.fromStart(400.dp)),
            initialAnchoredIndex = 0,
        )

    LaunchedEffect(uiState.selectedItem) {
        navigator.navigateTo(
            if (uiState.selectedItem != null) {
                ListDetailPaneScaffoldRole.Detail
            } else {
                ListDetailPaneScaffoldRole.List
            },
        )
    }

    backHandler(navigator.canNavigateBack(), { predictiveBackProgress = it }) { navigateBack() }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        scaffoldState = navigator.scaffoldState,
        paneExpansionState = paneExpansionState,
        paneExpansionDragHandle = { VerticalDivider() },
        modifier = modifier,
        listPane = {
            ReadoutListPane(
                uiState = uiState,
                onSimulatorSelected = onSimulatorSelected,
                onMove = onMove,
                onReadoutEnabledChanged = onReadoutEnabledChanged,
                onQueueEnabledChanged = onQueueEnabledChanged,
                onItemClick = onItemSelected,
                scrollToTopRequest = scrollToTopRequest,
            )
        },
        detailPane = {
            uiState.selectedItem?.let { selectedItem ->
                Box(modifier = Modifier.predictiveBackDetailPane(predictiveBackProgress)) {
                    ReadoutDetailPane(
                        title = selectedItemTitle(selectedItem),
                        canNavigateBack = navigator.canNavigateBack(),
                        onBack = { navigateBack() },
                        content = { detailContent(selectedItem) },
                    )
                }
            }
        },
    )
}

@Composable
private fun selectedItemTitle(selectedItem: ReadoutListItemType): String = readoutItemDisplayName(selectedItem.id.value)

@Preview(showBackground = true)
@Composable
private fun ReadoutContentPreview() {
    ReadoutContent(
        uiState =
            ReadoutListUiState(
                simulators = listOf(Simulator.LmuWindows),
                selectedSimulator = Simulator.LmuWindows,
                items =
                    listOf(
                        ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                        ReadoutItemKey.LmuWindows.Flag.Root,
                        ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                        ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                        ReadoutItemKey.LmuWindows.MyBestLap.Root,
                    ),
            ),
        onSimulatorSelected = {},
        onMove = { _, _ -> },
        onReadoutEnabledChanged = { _, _ -> },
        onQueueEnabledChanged = { _, _ -> },
        onItemSelected = {},
        onClearSelectedItem = {},
    )
}
