package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.launch
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListPane
import kurou.kodriver.feature.otherlist.OtherListUiState
import kurou.kodriver.feature.otherlist.OtherListViewModel
import kurou.kodriver.feature.otherlist.TelemetryLogResetConfirmDialog
import kurou.kodriver.feature.otherlist.generated.resources.Res
import kurou.kodriver.feature.otherlist.generated.resources.telemetry_log_reset_failure
import kurou.kodriver.feature.otherlist.generated.resources.telemetry_log_reset_success
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val GITHUB_REPOSITORY_URL = "https://github.com/ai-kurou/KoDriver"
private const val RELEASE_PAGE_URL = "$GITHUB_REPOSITORY_URL/releases"
internal const val TTS_ENGINE_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.google.android.tts"

/**
 * Other のコンテンツを表示する Composable。
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun OtherContent(
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()),
    windowPosture: Posture = currentWindowAdaptiveInfoV2().windowPosture,
    backHandler: AppBackHandler = { _, _, _ -> },
    scrollToTopRequest: Int = 0,
    onOpenReadoutStartSoundDialog: () -> Unit = {},
    onOpenThemeDialog: () -> Unit = {},
    onOpenOverlayTextColorDialog: () -> Unit = {},
    onOpenOverlayTextSizeDialog: () -> Unit = {},
    detailContent: @Composable (OtherListItemType, Boolean, () -> Unit, Long?, Long) -> Unit = { _, _, _, _, _ -> },
) {
    val viewModel: OtherListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    SideEffect(Unit) {
        viewModel.checkUpdate()
        viewModel.checkStartupEnabled()
    }

    LifecycleResumeEffect(Unit) {
        viewModel.checkAccessLocalNetworkPermission()
        onPauseOrDispose {}
    }

    OtherContent(
        uiState = uiState,
        onItemSelected = viewModel::onItemSelected,
        onResetClick = viewModel::onResetClick,
        onResetConfirm = viewModel::onResetConfirm,
        onResetDismiss = viewModel::onResetDismiss,
        onResetResultConsumed = viewModel::consumeResetResult,
        onOpenGitHubRepository = { uriHandler.openUri(GITHUB_REPOSITORY_URL) },
        onOpenReleasePage = { uriHandler.openUri(RELEASE_PAGE_URL) },
        onOpenWindowsSpeechSettings = viewModel::openWindowsSpeechSettings,
        onOpenTtsEngineInPlayStore = { uriHandler.openUri(TTS_ENGINE_PLAY_STORE_URL) },
        onOpenReadoutStartSoundDialog = onOpenReadoutStartSoundDialog,
        onOpenThemeDialog = onOpenThemeDialog,
        onOpenOverlayTextColorDialog = onOpenOverlayTextColorDialog,
        onOpenOverlayTextSizeDialog = onOpenOverlayTextSizeDialog,
        onOverlayVisibleChange = viewModel::onOverlayVisibleChange,
        onKeepScreenOnChange = viewModel::onKeepScreenOnChange,
        onDynamicColorEnabledChange = viewModel::onDynamicColorEnabledChange,
        onHapticFeedbackEnabledChange = viewModel::onHapticFeedbackEnabledChange,
        onStartupEnabledChange = viewModel::onStartupEnabledChange,
        onAppVersionTapped = { viewModel.selectItem(OtherListItemType.DebugState) },
        onClearSelectedItem = viewModel::clearSelectedItem,
        modifier = modifier,
        scaffoldDirective = scaffoldDirective,
        windowPosture = windowPosture,
        backHandler = backHandler,
        scrollToTopRequest = scrollToTopRequest,
        detailContent = detailContent,
    )
}

@Suppress("LongParameterList")
private fun handleOtherItemClick(
    itemType: OtherListItemType,
    onItemSelected: (OtherListItemType) -> Unit,
    onResetClick: () -> Unit,
    onOpenGitHubRepository: () -> Unit,
    onOpenReleasePage: () -> Unit,
    onOpenReadoutStartSoundDialog: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    onOpenOverlayTextColorDialog: () -> Unit,
    onOpenOverlayTextSizeDialog: () -> Unit,
    onOpenWindowsSpeechSettings: () -> Unit,
    onOpenTtsEngineInPlayStore: () -> Unit,
) {
    when (itemType) {
        OtherListItemType.DeleteAllLogs -> onResetClick()

        OtherListItemType.OverlayTextColor -> onOpenOverlayTextColorDialog()

        OtherListItemType.ReadoutStartSound -> onOpenReadoutStartSoundDialog()

        OtherListItemType.Theme -> onOpenThemeDialog()

        OtherListItemType.OverlayTextSize -> onOpenOverlayTextSizeDialog()

        OtherListItemType.GitHubRepository -> onOpenGitHubRepository()

        OtherListItemType.ReleasePage -> onOpenReleasePage()

        OtherListItemType.TtsEngineMissing -> onOpenTtsEngineInPlayStore()

        OtherListItemType.WindowsSpeechUnavailable -> onOpenWindowsSpeechSettings()

        OtherListItemType.AccessLocalNetworkPermission,
        OtherListItemType.ServerIp,
        OtherListItemType.ConsoleIp,
        OtherListItemType.Volume,
        OtherListItemType.Voice,
        OtherListItemType.VoiceSpeed,
        OtherListItemType.VoicePitch,
        OtherListItemType.AndroidAudioUsage,
        // TtsLanguageDataMissingはOtherListPane側でOS設定画面を直接開くため、ここには到達しない。
        OtherListItemType.TtsLanguageDataMissing,
        OtherListItemType.OverlayBackgroundOpacity,
        OtherListItemType.OverlayVisible,
        OtherListItemType.KeepScreenOn,
        OtherListItemType.DynamicColor,
        OtherListItemType.HapticFeedback,
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
    onResetClick: () -> Unit = {},
    onResetConfirm: () -> Unit = {},
    onResetDismiss: () -> Unit = {},
    onResetResultConsumed: () -> Unit = {},
    onOpenGitHubRepository: () -> Unit = {},
    onOpenReleasePage: () -> Unit = {},
    onOpenReadoutStartSoundDialog: () -> Unit = {},
    onOpenThemeDialog: () -> Unit = {},
    onOpenOverlayTextColorDialog: () -> Unit = {},
    onOpenOverlayTextSizeDialog: () -> Unit = {},
    onOpenWindowsSpeechSettings: () -> Unit = {},
    onOpenTtsEngineInPlayStore: () -> Unit = {},
    onOverlayVisibleChange: (Boolean) -> Unit = {},
    onKeepScreenOnChange: (Boolean) -> Unit = {},
    onDynamicColorEnabledChange: (Boolean) -> Unit = {},
    onHapticFeedbackEnabledChange: (Boolean) -> Unit = {},
    onStartupEnabledChange: (Boolean) -> Unit = {},
    onAppVersionTapped: () -> Unit = {},
    onClearSelectedItem: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()),
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass,
    windowPosture: Posture = currentWindowAdaptiveInfoV2().windowPosture,
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
            anchors = listOf(PaneExpansionAnchor.Offset.fromStart(AppPaneWidth)),
            initialAnchoredIndex = 0,
        )

    SideEffect(uiState.selectedItem) {
        navigationState.navigateTo(
            if (uiState.selectedItem != null) {
                OtherPaneDestination.Detail
            } else {
                OtherPaneDestination.List
            },
        )
    }
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

    val snackbarHostState = remember { SnackbarHostState() }
    OtherResetEffects(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onResetConfirm = onResetConfirm,
        onResetDismiss = onResetDismiss,
        onResetResultConsumed = onResetResultConsumed,
    )

    Box(modifier = modifier) {
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            scaffoldState = navigator.scaffoldState,
            paneExpansionState = paneExpansionState,
            paneExpansionDragHandle = { VerticalDivider() },
            modifier = Modifier.constrainToTabletopTopPane(windowPosture),
            listPane = {
                OtherListPane(
                    uiState = uiState,
                    onOverlayVisibleChange = onOverlayVisibleChange,
                    onKeepScreenOnChange = onKeepScreenOnChange,
                    onDynamicColorEnabledChange = onDynamicColorEnabledChange,
                    onHapticFeedbackEnabledChange = onHapticFeedbackEnabledChange,
                    onStartupEnabledChange = onStartupEnabledChange,
                    onAppVersionTapped = onAppVersionTapped,
                    onItemClick = { itemType ->
                        handleOtherItemClick(
                            itemType = itemType,
                            onItemSelected = onItemSelected,
                            onResetClick = onResetClick,
                            onOpenGitHubRepository = onOpenGitHubRepository,
                            onOpenReleasePage = onOpenReleasePage,
                            onOpenReadoutStartSoundDialog = onOpenReadoutStartSoundDialog,
                            onOpenThemeDialog = onOpenThemeDialog,
                            onOpenOverlayTextColorDialog = onOpenOverlayTextColorDialog,
                            onOpenOverlayTextSizeDialog = onOpenOverlayTextSizeDialog,
                            onOpenWindowsSpeechSettings = onOpenWindowsSpeechSettings,
                            onOpenTtsEngineInPlayStore = onOpenTtsEngineInPlayStore,
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
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun OtherResetEffects(
    uiState: OtherListUiState,
    snackbarHostState: SnackbarHostState,
    onResetConfirm: () -> Unit,
    onResetDismiss: () -> Unit,
    onResetResultConsumed: () -> Unit,
) {
    val successMessage = stringResource(Res.string.telemetry_log_reset_success)
    val failureMessage = stringResource(Res.string.telemetry_log_reset_failure)
    val currentOnResetResultConsumed by rememberUpdatedState(onResetResultConsumed)
    // タブ切替でスナックバー表示中にLaunchedEffectが中断されると結果が残り、戻った時に再表示されるため、離脱時に消費する。
    DisposableEffect(Unit) {
        onDispose { currentOnResetResultConsumed() }
    }
    LaunchedEffect(uiState.reset.resetSucceeded) {
        val succeeded = uiState.reset.resetSucceeded ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(if (succeeded) successMessage else failureMessage)
        currentOnResetResultConsumed()
    }
    if (uiState.reset.showResetConfirmDialog) {
        TelemetryLogResetConfirmDialog(onConfirm = onResetConfirm, onDismiss = onResetDismiss)
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherContentPreview() {
    AppTheme {
        OtherContent(
            uiState = OtherListUiState(),
            onItemSelected = {},
            onClearSelectedItem = {},
        )
    }
}
