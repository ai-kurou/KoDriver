package kurou.kodriver.feature.readout

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
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ReadoutContent(
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    backHandler: @Composable (Boolean, () -> Unit) -> Unit = { _, _ -> },
    viewModel: ReadoutViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateInLifecycle()
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>(
        scaffoldDirective = if (uiState.selectedItem == null && scaffoldDirective.maxHorizontalPartitions > 1)
            scaffoldDirective.copy(maxHorizontalPartitions = 1)
        else
            scaffoldDirective,
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
        viewModel.clearSelectedItem()
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
                onSimulatorSelected = viewModel::onSimulatorSelected,
                onMove = viewModel::moveItem,
                onReadoutEnabledChanged = viewModel::onReadoutEnabledChanged,
                onItemClick = { item ->
                    viewModel.onItemSelected(item)
                },
            )
        },
        detailPane = {
            ReadoutDetailPane(
                canNavigateBack = navigator.canNavigateBack(),
                onBack = { navigateBack() },
            )
        },
    )
}

@Preview
@Composable
fun ReadoutContentPreview() {
    ReadoutContent()
}
