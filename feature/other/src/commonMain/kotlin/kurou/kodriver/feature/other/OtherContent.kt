package kurou.kodriver.feature.other

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun OtherContent(
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    backHandler: @Composable (Boolean, () -> Unit) -> Unit = { _, _ -> },
    detailContent: @Composable (OtherItemType) -> Unit = {},
) {
    val viewModel: OtherViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    OtherContent(
        uiState = uiState,
        onItemSelected = viewModel::onItemSelected,
        onClearSelectedItem = viewModel::clearSelectedItem,
        modifier = modifier,
        scaffoldDirective = scaffoldDirective,
        backHandler = backHandler,
        detailContent = detailContent,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun OtherContent(
    uiState: OtherListUiState,
    onItemSelected: (String) -> Unit,
    onClearSelectedItem: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    backHandler: @Composable (Boolean, () -> Unit) -> Unit = { _, _ -> },
    detailContent: @Composable (OtherItemType) -> Unit = {},
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>(
        scaffoldDirective = if (uiState.selectedItem == null && scaffoldDirective.maxHorizontalPartitions > 1) {
            scaffoldDirective.copy(maxHorizontalPartitions = 1)
        } else {
            scaffoldDirective
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
            if (uiState.selectedItem != null) {
                ListDetailPaneScaffoldRole.Detail
            } else {
                ListDetailPaneScaffoldRole.List
            },
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
            OtherListPane(
                uiState = uiState,
                onItemClick = onItemSelected,
            )
        },
        detailPane = {
            uiState.selectedItem?.let { selectedItem ->
                OtherDetailPane(
                    canNavigateBack = navigator.canNavigateBack(),
                    onBack = { navigateBack() },
                ) {
                    detailContent(selectedItem)
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherContentPreview() {
    OtherContent(
        uiState = OtherListUiState(),
        onItemSelected = {},
        onClearSelectedItem = {},
    )
}
