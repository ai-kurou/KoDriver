package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.Posture
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.launch
import kurou.kodriver.core.designsystem.AppBackHandler
import kurou.kodriver.core.designsystem.KoDriverListPaneWidth
import kurou.kodriver.core.designsystem.ScrollToTopEffect
import kurou.kodriver.core.designsystem.constrainToTabletopTopPane
import kurou.kodriver.core.designsystem.predictiveBackDetailPane
import kurou.kodriver.core.designsystem.shouldCollapseDetailPane
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ManualReadoutContent(
    simulatorId: String,
    simulatorName: String,
    modifier: Modifier = Modifier,
    tabReselectRequest: Int = 0,
    backHandler: AppBackHandler = { _, _, _ -> },
    detailContent: @Composable (setId: String, canNavigateBack: Boolean, onBack: () -> Unit) -> Unit,
) {
    val viewModel: ManualReadoutListViewModel =
        koinViewModel(key = simulatorId, parameters = { parametersOf(simulatorId) })
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var scrollToTopRequest by remember { mutableIntStateOf(0) }
    // 再タップ回数は AppScreen 側で保持され続けるため、タブ再表示時の初期値は再タップとして扱わない。
    val initialTabReselectRequest = remember { tabReselectRequest }
    SideEffect(tabReselectRequest) {
        if (tabReselectRequest > initialTabReselectRequest) {
            handleManualReadoutTabReselected(
                selectedSetId = uiState.selectedSetId,
                clearSelectedSet = viewModel::clearSelectedSet,
                requestScrollToTop = { scrollToTopRequest++ },
            )
        }
    }
    ManualReadoutContentScaffold(
        uiState = uiState,
        simulatorId = simulatorId,
        scrollToTopRequest = scrollToTopRequest,
        simulatorName = simulatorName,
        onAddSet = viewModel::addSet,
        onReadSet = viewModel::readSet,
        onDeleteSet = viewModel::deleteSet,
        onSetClick = viewModel::selectSet,
        onClearSelectedSet = viewModel::clearSelectedSet,
        modifier = modifier,
        backHandler = backHandler,
        detailContent = detailContent,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun ManualReadoutContentScaffold(
    uiState: ManualReadoutListUiState,
    simulatorId: String,
    simulatorName: String,
    onAddSet: () -> Unit,
    onReadSet: (String) -> Unit,
    onSetClick: (String) -> Unit,
    onClearSelectedSet: () -> Unit,
    modifier: Modifier = Modifier,
    onDeleteSet: (String) -> Unit = {},
    scrollToTopRequest: Int = 0,
    listState: LazyListState = rememberLazyListState(),
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
    windowPosture: Posture = currentWindowAdaptiveInfo().windowPosture,
    backHandler: AppBackHandler = { _, _, _ -> },
    detailContent: @Composable (setId: String, canNavigateBack: Boolean, onBack: () -> Unit) -> Unit,
) {
    ScrollToTopEffect(scrollToTopRequest) { listState.animateScrollToItem(0) }
    val navigationState =
        rememberManualReadoutNavigationState(
            initial =
                if (uiState.selectedSetId !=
                    null
                ) {
                    ManualReadoutPaneDestination.Detail
                } else {
                    ManualReadoutPaneDestination.List
                },
        )
    val navigator =
        rememberListDetailPaneScaffoldNavigator<Nothing>(
            scaffoldDirective =
                when {
                    uiState.selectedSetId == null && scaffoldDirective.maxHorizontalPartitions > 1 -> {
                        scaffoldDirective.copy(maxHorizontalPartitions = 1)
                    }

                    uiState.selectedSetId != null &&
                        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> {
                        scaffoldDirective.copy(maxHorizontalPartitions = 2)
                    }

                    else -> {
                        scaffoldDirective
                    }
                },
            initialDestinationHistory =
                if (uiState.selectedSetId != null) {
                    listOf(
                        ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List),
                        ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail),
                    )
                } else {
                    listOf(ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List))
                },
        )
    val paneExpansionState =
        rememberPaneExpansionState(
            anchors = listOf(PaneExpansionAnchor.Offset.fromStart(KoDriverListPaneWidth)),
            initialAnchoredIndex = 0,
        )
    val scope = rememberCoroutineScope()
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    val navigateBack = {
        predictiveBackProgress = 0f
        scope.launch { navigator.navigateBack() }
        navigationState.navigateTo(ManualReadoutPaneDestination.List)
        onClearSelectedSet()
    }

    SideEffect(uiState.selectedSetId) {
        navigationState.navigateTo(
            if (uiState.selectedSetId != null) {
                ManualReadoutPaneDestination.Detail
            } else {
                ManualReadoutPaneDestination.List
            },
        )
    }
    LaunchedEffect(uiState.selectedSetId) {
        navigator.navigateTo(
            if (uiState.selectedSetId != null) ListDetailPaneScaffoldRole.Detail else ListDetailPaneScaffoldRole.List,
        )
    }

    // テーブルトップ姿勢や、縦ヒンジが完全には平らに開いていない姿勢では、
    // detailPaneを表示する幅が確保できず潰れて表示されてしまうため、
    // listPaneの選択を解除して一覧のみの表示に戻す。
    val currentOnClearSelectedSet by rememberUpdatedState(onClearSelectedSet)
    SideEffect(windowPosture.shouldCollapseDetailPane) {
        if (windowPosture.shouldCollapseDetailPane) {
            currentOnClearSelectedSet()
        }
    }

    backHandler(navigator.canNavigateBack(), { predictiveBackProgress = it }) { navigateBack() }

    Box(modifier = modifier.constrainToTabletopTopPane(windowPosture)) {
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            scaffoldState = navigator.scaffoldState,
            paneExpansionState = paneExpansionState,
            paneExpansionDragHandle = { VerticalDivider() },
            modifier = Modifier.fillMaxSize(),
            listPane = {
                ManualReadoutListPaneContent(
                    uiState = uiState,
                    simulatorId = simulatorId,
                    listState = listState,
                    simulatorName = simulatorName,
                    onAddSet = onAddSet,
                    onReadSet = onReadSet,
                    onDeleteSet = onDeleteSet,
                    onSetClick = onSetClick,
                )
            },
            detailPane = {
                uiState.selectedSetId?.let { setId ->
                    Box(modifier = Modifier.predictiveBackDetailPane(predictiveBackProgress)) {
                        key(setId) { detailContent(setId, navigator.canNavigateBack(), navigateBack) }
                    }
                }
            },
        )
    }
}

internal fun handleManualReadoutTabReselected(
    selectedSetId: String?,
    clearSelectedSet: () -> Unit,
    requestScrollToTop: () -> Unit,
) {
    if (selectedSetId != null) {
        clearSelectedSet()
    } else {
        requestScrollToTop()
    }
}
