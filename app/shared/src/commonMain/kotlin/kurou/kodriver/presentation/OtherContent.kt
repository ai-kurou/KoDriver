package kurou.kodriver.presentation

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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.otherlist.generated.resources.Res
import kodriver.feature.otherlist.generated.resources.item_license
import kotlinx.coroutines.launch
import kurou.kodriver.feature.other.OtherDetailPane
import kurou.kodriver.feature.other.OtherItemType
import kurou.kodriver.feature.other.OtherListPane
import kurou.kodriver.feature.other.OtherListUiState
import kurou.kodriver.feature.other.OtherViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val GITHUB_REPOSITORY_URL = "https://github.com/ai-kurou/KoDriver"
private const val RELEASE_PAGE_URL = "$GITHUB_REPOSITORY_URL/releases"

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
    val uriHandler = LocalUriHandler.current
    OtherContent(
        uiState = uiState,
        onItemSelected = viewModel::onItemSelected,
        onOpenGitHubRepository = { uriHandler.openUri(GITHUB_REPOSITORY_URL) },
        onOpenReleasePage = { uriHandler.openUri(RELEASE_PAGE_URL) },
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
    onOpenGitHubRepository: () -> Unit = {},
    onOpenReleasePage: () -> Unit = {},
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
                onItemClick = { itemId ->
                    when (itemId) {
                        OtherItemType.GitHubRepository.id -> onOpenGitHubRepository()
                        OtherItemType.ReleasePage.id -> onOpenReleasePage()
                        else -> onItemSelected(itemId)
                    }
                },
            )
        },
        detailPane = {
            uiState.selectedItem?.let { selectedItem ->
                val title = when (selectedItem) {
                    OtherItemType.License -> stringResource(Res.string.item_license)
                    else -> selectedItem.id
                }
                OtherDetailPane(
                    title = title,
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
