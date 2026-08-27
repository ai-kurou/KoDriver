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
        onMove = viewModel::moveItem,
        onReadoutEnabledChanged = viewModel::onReadoutEnabledChanged,
        onQueueEnabledChanged = viewModel::onQueueEnabledChanged,
        onStartSoundEnabledChanged = viewModel::onStartSoundEnabledChanged,
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
    onMove: (Int, Int) -> Unit,
    onReadoutEnabledChanged: (ReadoutItemKey, Boolean) -> Unit,
    onQueueEnabledChanged: (ReadoutItemKey, Boolean) -> Unit,
    onStartSoundEnabledChanged: (ReadoutItemKey, Boolean) -> Unit,
    onItemSelected: (ReadoutItemKey) -> Unit,
    onClearSelectedItem: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
    backHandler: AppBackHandler = { _, _, _ -> },
    scrollToTopRequest: Int = 0,
    detailContent: @Composable (ReadoutListItemType) -> Unit = {},
) {
    val navigationState =
        rememberReadoutNavigationState(
            initial = if (uiState.selectedItem != null) ReadoutPaneDestination.Detail else ReadoutPaneDestination.List,
        )
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
        navigationState.navigateTo(ReadoutPaneDestination.List)
        onClearSelectedItem()
    }
    val paneExpansionState =
        rememberPaneExpansionState(
            anchors = listOf(PaneExpansionAnchor.Offset.fromStart(400.dp)),
            initialAnchoredIndex = 0,
        )

    LaunchedEffect(uiState.selectedItem) {
        navigationState.navigateTo(
            if (uiState.selectedItem != null) {
                ReadoutPaneDestination.Detail
            } else {
                ReadoutPaneDestination.List
            },
        )
        navigator.navigateTo(
            if (navigationState.current == ReadoutPaneDestination.Detail) {
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
                onMove = onMove,
                onReadoutEnabledChanged = onReadoutEnabledChanged,
                onQueueEnabledChanged = onQueueEnabledChanged,
                onStartSoundEnabledChanged = onStartSoundEnabledChanged,
                onItemClick = onItemSelected,
                scrollToTopRequest = scrollToTopRequest,
            )
        },
        detailPane = {
            uiState.selectedItem?.let { selectedItem ->
                Box(modifier = Modifier.predictiveBackDetailPane(predictiveBackProgress)) {
                    ReadoutDetailPane(
                        title = itemDisplayName(selectedItem.id),
                        canNavigateBack = navigator.canNavigateBack(),
                        onBack = { navigateBack() },
                        content = { detailContent(selectedItem) },
                    )
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun ReadoutContentPreview() {
    ReadoutContent(
        uiState =
            ReadoutListUiState(
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
        onMove = { _, _ -> },
        onReadoutEnabledChanged = { _, _ -> },
        onQueueEnabledChanged = { _, _ -> },
        onStartSoundEnabledChanged = { _, _ -> },
        onItemSelected = {},
        onClearSelectedItem = {},
    )
}
