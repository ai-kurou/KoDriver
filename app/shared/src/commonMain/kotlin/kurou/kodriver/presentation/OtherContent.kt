package kurou.kodriver.presentation

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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.launch
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListPane
import kurou.kodriver.feature.otherlist.OtherListUiState
import kurou.kodriver.feature.otherlist.OtherListViewModel
import org.koin.compose.viewmodel.koinViewModel

private const val GITHUB_REPOSITORY_URL = "https://github.com/ai-kurou/KoDriver"
private const val RELEASE_PAGE_URL = "$GITHUB_REPOSITORY_URL/releases"

/**
 * Other のコンテンツを表示する Composable。
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun OtherContent(
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    backHandler: AppBackHandler = { _, _, _ -> },
    scrollToTopRequest: Int = 0,
    onOpenReadoutStartSoundDialog: () -> Unit = {},
    onOpenThemeDialog: () -> Unit = {},
    detailContent: @Composable (OtherListItemType, Boolean, () -> Unit, Long?, Long) -> Unit = { _, _, _, _, _ -> },
) {
    val viewModel: OtherListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        viewModel.checkUpdate()
        viewModel.checkStartupEnabled()
    }

    OtherContent(
        uiState = uiState,
        onItemSelected = viewModel::onItemSelected,
        onOpenGitHubRepository = { uriHandler.openUri(GITHUB_REPOSITORY_URL) },
        onOpenReleasePage = { uriHandler.openUri(RELEASE_PAGE_URL) },
        onOpenReadoutStartSoundDialog = onOpenReadoutStartSoundDialog,
        onOpenThemeDialog = onOpenThemeDialog,
        onKeepScreenOnChange = viewModel::onKeepScreenOnChange,
        onDynamicColorEnabledChange = viewModel::onDynamicColorEnabledChange,
        onStartupEnabledChange = viewModel::onStartupEnabledChange,
        onAppVersionTapped = { viewModel.selectItem(OtherListItemType.DebugState) },
        onClearSelectedItem = viewModel::clearSelectedItem,
        modifier = modifier,
        scaffoldDirective = scaffoldDirective,
        backHandler = backHandler,
        scrollToTopRequest = scrollToTopRequest,
        detailContent = detailContent,
    )
}

private fun handleOtherItemClick(
    itemType: OtherListItemType,
    onItemSelected: (OtherListItemType) -> Unit,
    onOpenGitHubRepository: () -> Unit,
    onOpenReleasePage: () -> Unit,
    onOpenReadoutStartSoundDialog: () -> Unit,
    onOpenThemeDialog: () -> Unit,
) {
    when (itemType) {
        OtherListItemType.ReadoutStartSound -> onOpenReadoutStartSoundDialog()

        OtherListItemType.Theme -> onOpenThemeDialog()

        OtherListItemType.GitHubRepository -> onOpenGitHubRepository()

        OtherListItemType.ReleasePage -> onOpenReleasePage()

        OtherListItemType.ServerIp,
        OtherListItemType.ConsoleIp,
        OtherListItemType.Volume,
        OtherListItemType.KeepScreenOn,
        OtherListItemType.DynamicColor,
        OtherListItemType.Startup,
        OtherListItemType.Feedback,
        OtherListItemType.License,
        OtherListItemType.DebugState,
        -> onItemSelected(itemType)
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun OtherContent(
    uiState: OtherListUiState,
    onItemSelected: (OtherListItemType) -> Unit,
    onOpenGitHubRepository: () -> Unit = {},
    onOpenReleasePage: () -> Unit = {},
    onOpenReadoutStartSoundDialog: () -> Unit = {},
    onOpenThemeDialog: () -> Unit = {},
    onKeepScreenOnChange: (Boolean) -> Unit = {},
    onDynamicColorEnabledChange: (Boolean) -> Unit = {},
    onStartupEnabledChange: (Boolean) -> Unit = {},
    onAppVersionTapped: () -> Unit = {},
    onClearSelectedItem: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
    backHandler: AppBackHandler = { _, _, _ -> },
    scrollToTopRequest: Int = 0,
    detailContent: @Composable (OtherListItemType, Boolean, () -> Unit, Long?, Long) -> Unit = { _, _, _, _, _ -> },
) {
    val navigationState =
        rememberOtherNavigationState(
            initial = if (uiState.selectedItem != null) OtherPaneDestination.Detail else OtherPaneDestination.List,
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
        navigationState.navigateTo(OtherPaneDestination.List)
        onClearSelectedItem()
        Unit
    }
    val paneExpansionState =
        rememberPaneExpansionState(
            anchors = listOf(PaneExpansionAnchor.Offset.fromStart(400.dp)),
            initialAnchoredIndex = 0,
        )

    LaunchedEffect(uiState.selectedItem) {
        navigationState.navigateTo(
            if (uiState.selectedItem != null) {
                OtherPaneDestination.Detail
            } else {
                OtherPaneDestination.List
            },
        )
        navigator.navigateTo(
            if (navigationState.current == OtherPaneDestination.Detail) {
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
            OtherListPane(
                uiState = uiState,
                onKeepScreenOnChange = onKeepScreenOnChange,
                onDynamicColorEnabledChange = onDynamicColorEnabledChange,
                onStartupEnabledChange = onStartupEnabledChange,
                onAppVersionTapped = onAppVersionTapped,
                onItemClick = { itemType ->
                    handleOtherItemClick(
                        itemType = itemType,
                        onItemSelected = onItemSelected,
                        onOpenGitHubRepository = onOpenGitHubRepository,
                        onOpenReleasePage = onOpenReleasePage,
                        onOpenReadoutStartSoundDialog = onOpenReadoutStartSoundDialog,
                        onOpenThemeDialog = onOpenThemeDialog,
                    )
                },
                scrollToTopRequest = scrollToTopRequest,
            )
        },
        detailPane = {
            uiState.selectedItem?.let { selectedItem ->
                Box(modifier = Modifier.predictiveBackDetailPane(predictiveBackProgress)) {
                    detailContent(
                        selectedItem,
                        navigator.canNavigateBack(),
                        navigateBack,
                        uiState.selectedFeedbackTelemetryLogId,
                        uiState.feedbackAttachRequestId,
                    )
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
