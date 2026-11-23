package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.unit.IntRect
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
                }.withoutAutoFocusOnCurrentDestination(),
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
    // 戻るときは選択がすぐ解除されるため、詳細の退場アニメーション中も描画できるよう、直前に選ばれた項目を覚えておく
    var lastSelectedItem by remember { mutableStateOf<AutoReadoutListItemType?>(null) }
    LaunchedEffect(uiState.selectedItem) {
        uiState.selectedItem?.let { lastSelectedItem = it }
    }
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
                AnimatedPane(
                    enterTransition = PaneEnterTransition,
                    exitTransition = PaneExitTransition,
                    boundsAnimationSpec = PaneBoundsAnimationSpec,
                ) {
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
                (uiState.selectedItem ?: lastSelectedItem.takeIf { it.hasBackTransition() })?.let { selectedItem ->
                    AnimatedPane(
                        enterTransition = PaneEnterTransition,
                        exitTransition = PaneExitTransition,
                        boundsAnimationSpec = PaneBoundsAnimationSpec,
                    ) {
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

/**
 * 詳細から一覧へ戻るときにも共有要素のアニメーションを付ける項目か。
 * 試験導入のため、現在はLMUの自己ベストラップのみ。
 */
private fun AutoReadoutListItemType?.hasBackTransition(): Boolean = this == LmuWindowsAutoReadoutListItemType.MyBestLap

private const val PANE_TRANSITION_MILLIS = 250

/**
 * ペインの出入りはフェードにする。
 * 既定は減衰比0.8のバネでペインを横へスライドさせるため、目標位置を行き過ぎて左右に揺れて見える。
 */
private val PaneEnterTransition = fadeIn(animationSpec = tween(durationMillis = PANE_TRANSITION_MILLIS))
private val PaneExitTransition = fadeOut(animationSpec = tween(durationMillis = PANE_TRANSITION_MILLIS))

/** ペインが表示されたまま大きさや位置が変わるときの動き。既定のバネと違い、行き過ぎない。 */
private val PaneBoundsAnimationSpec = tween<IntRect>(durationMillis = PANE_TRANSITION_MILLIS)

/**
 * 現在のペインへ scaffold が自動でフォーカスを移す動きを止めた [PaneScaffoldDirective] を返す。
 *
 * [AnimatedPane] を使うと、詳細へ移るたびにペイン内の最初のフォーカス可能な部品へフォーカスが移る。
 * 詳細の下部にあるスライダーなどが選ばれると、画面内へ出すためのスクロールで TopAppBar が畳まれて戻るボタンが押せなくなる。
 * キーボード操作の初期フォーカスは [kurou.kodriver.core.designsystem.detailPaneInitialFocus] が受け持つため、ここでは止める。
 * [PaneScaffoldDirective.copy] はこの設定を引き継がないため、コンストラクターで作り直す。
 */
private fun PaneScaffoldDirective.withoutAutoFocusOnCurrentDestination(): PaneScaffoldDirective =
    PaneScaffoldDirective(
        maxHorizontalPartitions = maxHorizontalPartitions,
        horizontalPartitionSpacerSize = horizontalPartitionSpacerSize,
        maxVerticalPartitions = maxVerticalPartitions,
        verticalPartitionSpacerSize = verticalPartitionSpacerSize,
        defaultPanePreferredWidth = defaultPanePreferredWidth,
        defaultPanePreferredHeight = defaultPanePreferredHeight,
        excludedBounds = excludedBounds,
        shouldAutoFocusCurrentDestination = false,
    )

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
