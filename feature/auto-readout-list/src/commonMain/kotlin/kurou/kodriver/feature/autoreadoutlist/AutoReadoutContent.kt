package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.AnimatedPane
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.launch
import kurou.kodriver.core.designsystem.AppBackHandler
import kurou.kodriver.core.designsystem.KoDriverListPaneWidth
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.constrainToTabletopTopPane
import kurou.kodriver.core.designsystem.predictiveBackDetailPane
import kurou.kodriver.core.designsystem.shouldCollapseDetailPane
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.readoutEnabled
import org.koin.compose.viewmodel.koinViewModel

/**
 * Readout のコンテンツを表示する Composable。
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
public fun AutoReadoutContent(
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()),
    windowPosture: Posture = currentWindowAdaptiveInfoV2().windowPosture,
    backHandler: AppBackHandler = { _, _, _ -> },
    scrollToTopRequest: Int = 0,
    detailContent: @Composable (AutoReadoutListItemType) -> Unit = {},
) {
    val viewModel: AutoReadoutListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateInLifecycle()
    AutoReadoutContent(
        uiState = uiState,
        onMove = viewModel::moveItem,
        onReadoutEnabledChanged = viewModel::onReadoutEnabledChanged,
        onQueueEnabledChanged = viewModel::onQueueEnabledChanged,
        onStartSoundEnabledChanged = viewModel::onStartSoundEnabledChanged,
        onItemSelected = viewModel::onItemSelected,
        onClearSelectedItem = viewModel::clearSelectedItem,
        modifier = modifier,
        scaffoldDirective = scaffoldDirective,
        windowPosture = windowPosture,
        backHandler = backHandler,
        scrollToTopRequest = scrollToTopRequest,
        detailContent = detailContent,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun AutoReadoutContent(
    uiState: AutoReadoutListUiState,
    onMove: (Int, Int) -> Unit,
    onReadoutEnabledChanged: (ReadoutItemKey, Boolean) -> Unit,
    onQueueEnabledChanged: (ReadoutItemKey, Boolean) -> Unit,
    onStartSoundEnabledChanged: (ReadoutItemKey, Boolean) -> Unit,
    onItemSelected: (ReadoutItemKey) -> Unit,
    onClearSelectedItem: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()),
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass,
    windowPosture: Posture = currentWindowAdaptiveInfoV2().windowPosture,
    backHandler: AppBackHandler = { _, _, _ -> },
    scrollToTopRequest: Int = 0,
    detailContent: @Composable (AutoReadoutListItemType) -> Unit = {},
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
    // 詳細を閉じて一覧へ戻ったとき、直前に開いた行へフォーカスを戻すために、開いた項目のキーを覚えておく
    var lastOpenedItemKey by remember { mutableStateOf<String?>(null) }
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    val navigateBack = {
        predictiveBackProgress = 0f
        scope.launch { navigator.navigateBack() }
        onClearSelectedItem()
    }
    val paneExpansionState =
        rememberPaneExpansionState(
            anchors = listOf(PaneExpansionAnchor.Offset.fromStart(KoDriverListPaneWidth)),
            initialAnchoredIndex = 0,
        )

    LaunchedEffect(uiState.selectedItem) {
        navigator.navigateTo(
            if (uiState.selectedItem != null) ListDetailPaneScaffoldRole.Detail else ListDetailPaneScaffoldRole.List,
        )
    }

    // テーブルトップ姿勢や、縦ヒンジが完全には平らに開いていない姿勢では、
    // detailPaneを表示する幅が確保できず潰れて表示されてしまうため、
    // listPaneの選択を解除して一覧のみの表示に戻す。
    val currentOnClearSelectedItem by rememberUpdatedState(onClearSelectedItem)
    SideEffect(windowPosture.shouldCollapseDetailPane) {
        if (windowPosture.shouldCollapseDetailPane) {
            currentOnClearSelectedItem()
        }
    }

    backHandler(navigator.canNavigateBack(), { predictiveBackProgress = it }) { navigateBack() }

    // 共有要素は一覧と詳細を同時に表示しない1ペイン表示のときだけ使う。2ペイン表示では遷移が起きない。
    val useSharedBounds = navigator.scaffoldDirective.maxHorizontalPartitions == 1
    SharedTransitionLayout(modifier = modifier.constrainToTabletopTopPane(windowPosture)) {
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            scaffoldState = navigator.scaffoldState,
            paneExpansionState = paneExpansionState,
            paneExpansionDragHandle = { VerticalDivider() },
            listPane = {
                AnimatedPane {
                    AutoReadoutListPane(
                        uiState = uiState,
                        onMove = onMove,
                        onReadoutEnabledChanged = onReadoutEnabledChanged,
                        onQueueEnabledChanged = onQueueEnabledChanged,
                        onStartSoundEnabledChanged = onStartSoundEnabledChanged,
                        onItemClick = {
                            lastOpenedItemKey = it.value
                            onItemSelected(it)
                        },
                        scrollToTopRequest = scrollToTopRequest,
                        restoreFocusItemKey = lastOpenedItemKey.takeIf { uiState.selectedItem == null },
                        onFocusRestored = { lastOpenedItemKey = null },
                        sharedBounds =
                            if (useSharedBounds) AutoReadoutSharedBounds(this@SharedTransitionLayout, this) else null,
                    )
                }
            },
            detailPane = {
                uiState.selectedItem?.let { selectedItem ->
                    AnimatedPane {
                        Box(modifier = Modifier.predictiveBackDetailPane(predictiveBackProgress)) {
                            AutoReadoutDetailPane(
                                title = autoReadoutItemDisplayName(selectedItem.id),
                                rootItemName = autoReadoutItemDisplayName(selectedItem.id),
                                background = autoReadoutBackground(selectedItem.id),
                                canNavigateBack = navigator.canNavigateBack(),
                                onBack = { navigateBack() },
                                rootEnabled = uiState.autoReadoutEnabledStates.readoutEnabled(selectedItem.id),
                                onEnableRoot = { onReadoutEnabledChanged(selectedItem.id, true) },
                                topBarModifier =
                                    if (useSharedBounds) {
                                        AutoReadoutSharedBounds(this@SharedTransitionLayout, this@AnimatedPane)
                                            .modifier(selectedItem.id)
                                    } else {
                                        Modifier
                                    },
                                content = { detailContent(selectedItem) },
                            )
                        }
                    }
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AutoReadoutContentPreview() {
    KoDriverTheme {
        AutoReadoutContent(
            uiState =
                AutoReadoutListUiState(
                    selectedSimulator = Simulator.LmuWindows,
                    items =
                        listOf(
                            LmuWindowsReadoutItemKey.VehicleApproach.Root,
                            LmuWindowsReadoutItemKey.Flag.Root,
                            LmuWindowsReadoutItemKey.VehicleDamage.Root,
                            LmuWindowsReadoutItemKey.TyreTemperature.Root,
                            LmuWindowsReadoutItemKey.MyBestLap.Root,
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
}
