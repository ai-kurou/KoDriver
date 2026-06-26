package kurou.kodriver.feature.readoutlist

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kodriver.feature.readoutlist.generated.resources.Res
import kodriver.feature.readoutlist.generated.resources.item_flag
import kodriver.feature.readoutlist.generated.resources.item_my_best_lap
import kodriver.feature.readoutlist.generated.resources.item_remaining_fuel_laps
import kodriver.feature.readoutlist.generated.resources.item_vehicle_approach
import kodriver.feature.readoutlist.generated.resources.item_vehicle_damage
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ReadoutContent(
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    backHandler: @Composable (Boolean, () -> Unit) -> Unit = { _, _ -> },
    detailContent: @Composable (ReadoutListItemType) -> Unit = {},
) {
    val viewModel: ReadoutListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateInLifecycle()
    ReadoutContent(
        uiState = uiState,
        onSimulatorSelected = viewModel::onSimulatorSelected,
        onMove = viewModel::moveItem,
        onReadoutEnabledChanged = viewModel::onReadoutEnabledChanged,
        onItemSelected = viewModel::onItemSelected,
        onClearSelectedItem = viewModel::clearSelectedItem,
        modifier = modifier,
        scaffoldDirective = scaffoldDirective,
        backHandler = backHandler,
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
    onItemSelected: (ReadoutItemKey) -> Unit,
    onClearSelectedItem: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
    backHandler: @Composable (Boolean, () -> Unit) -> Unit = { _, _ -> },
    detailContent: @Composable (ReadoutListItemType) -> Unit = {},
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>(
        scaffoldDirective = when {
            uiState.selectedItem == null && scaffoldDirective.maxHorizontalPartitions > 1 ->
                scaffoldDirective.copy(maxHorizontalPartitions = 1)
            uiState.selectedItem != null &&
                windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
                scaffoldDirective.copy(maxHorizontalPartitions = 2)
            else -> scaffoldDirective
        },
        initialDestinationHistory = if (uiState.selectedItem != null) {
            listOf(
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List),
                ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail),
            )
        } else {
            listOf(ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List))
        },
    )
    val scope = rememberCoroutineScope()
    val navigateBack = {
        scope.launch { navigator.navigateBack() }
        onClearSelectedItem()
    }
    val paneExpansionState = rememberPaneExpansionState(
        anchors = listOf(PaneExpansionAnchor.Offset.fromStart(350.dp)),
        initialAnchoredIndex = 0,
    )

    LaunchedEffect(uiState.selectedItem) {
        navigator.navigateTo(
            if (uiState.selectedItem != null)
                ListDetailPaneScaffoldRole.Detail
            else
                ListDetailPaneScaffoldRole.List,
        )
    }

    backHandler(navigator.canNavigateBack()) { navigateBack() }

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
                onItemClick = onItemSelected,
            )
        },
        detailPane = {
            uiState.selectedItem?.let { selectedItem ->
                val title = when (selectedItem) {
                    ReadoutListItemType.LmuWindows.VehicleApproach -> stringResource(Res.string.item_vehicle_approach)
                    ReadoutListItemType.LmuWindows.Flag -> stringResource(Res.string.item_flag)
                    ReadoutListItemType.LmuWindows.VehicleDamage -> stringResource(Res.string.item_vehicle_damage)
                    ReadoutListItemType.Gt7Ps5.MyBestLap -> stringResource(Res.string.item_my_best_lap)
                    ReadoutListItemType.Gt7Ps5.RemainingFuelLaps ->
                        stringResource(Res.string.item_remaining_fuel_laps)
                }
                ReadoutDetailPane(
                    title = title,
                    canNavigateBack = navigator.canNavigateBack(),
                    onBack = { navigateBack() },
                    content = { detailContent(selectedItem) },
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun ReadoutContentPreview() {
    ReadoutContent(
        uiState = ReadoutListUiState(
            simulators = listOf(Simulator.LmuWindows),
            selectedSimulator = Simulator.LmuWindows,
            items = listOf(
                ReadoutItemKey.VehicleApproach,
                ReadoutItemKey.Flag,
                ReadoutItemKey.VehicleDamage,
            ),
        ),
        onSimulatorSelected = {},
        onMove = { _, _ -> },
        onReadoutEnabledChanged = { _, _ -> },
        onItemSelected = {},
        onClearSelectedItem = {},
    )
}
