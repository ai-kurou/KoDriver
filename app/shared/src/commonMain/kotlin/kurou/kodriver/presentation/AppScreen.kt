package kurou.kodriver.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.app.shared.generated.resources.Res
import kurou.kodriver.app.shared.generated.resources.banner_access_local_network_permission_required
import kurou.kodriver.feature.autoreadoutlist.AceWindowsAutoReadoutListItemType
import kurou.kodriver.feature.autoreadoutlist.AutoReadoutContent
import kurou.kodriver.feature.autoreadoutlist.AutoReadoutListItemType
import kurou.kodriver.feature.autoreadoutlist.AutoReadoutListViewModel
import kurou.kodriver.feature.autoreadoutlist.Gt7Ps5AutoReadoutListItemType
import kurou.kodriver.feature.autoreadoutlist.LmuWindowsAutoReadoutListItemType
import kurou.kodriver.feature.main.AppScreenViewModel
import kurou.kodriver.feature.main.LowVolumeWarningUiState
import kurou.kodriver.feature.main.LowVolumeWarningViewModel
import kurou.kodriver.feature.main.SPEECH_RECOGNITION_CHIP_OVERLAY_INSET
import kurou.kodriver.feature.main.appScreenPrimarySimulatorLabel
import kurou.kodriver.feature.main.rememberSpeechRecognition
import kurou.kodriver.feature.main.speechRecognitionChipOverlayInset
import kurou.kodriver.feature.manualreadoutdetail.ManualReadoutSetDetailContent
import kurou.kodriver.feature.manualreadoutlist.ManualReadoutContent
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListViewModel
import kurou.kodriver.feature.otherlist.TtsUnavailableGuidance
import kurou.kodriver.feature.otherlist.rememberOpenTtsSettings
import kurou.kodriver.feature.speechrecognitionlist.SpeechRecognitionListPane
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** 下部ナビバー表示時に、ナビバーの直上へ重ねる要素（Snackbar・音声認識チップ）の下余白。 */
private val NAVIGATION_BAR_OVERLAY_BOTTOM_OFFSET = 96.dp

private fun withTabSwitch(
    action: (() -> Unit)?,
    switchToMore: () -> Unit,
): (() -> Unit)? =
    if (action != null) {
        {
            switchToMore()
            action()
        }
    } else {
        null
    }

internal fun ConnectionBannerNavigationTarget.toOtherListItemType(): OtherListItemType =
    when (this) {
        ConnectionBannerNavigationTarget.ConsoleIp -> OtherListItemType.ConsoleIp
        ConnectionBannerNavigationTarget.ServerIp -> OtherListItemType.ServerIp
        ConnectionBannerNavigationTarget.AccessLocalNetworkPermission -> OtherListItemType.AccessLocalNetworkPermission
    }

@Composable
private fun AppDestinationContent(
    destination: AppDestination,
    autoReadoutContent: @Composable () -> Unit,
    manualReadoutContent: @Composable () -> Unit,
    speechRecognitionContent: @Composable () -> Unit,
    otherContent: @Composable () -> Unit,
) {
    when (destination) {
        AppDestination.AutoReadout -> autoReadoutContent()
        AppDestination.ManualReadout -> manualReadoutContent()
        AppDestination.SpeechRecognition -> speechRecognitionContent()
        AppDestination.More -> otherContent()
    }
}

/**
 * AppScreen を提供する公開関数。
 */
@Composable
fun AppScreen(
    viewModel: AppScreenViewModel = koinViewModel(),
    autoReadoutListViewModel: AutoReadoutListViewModel = koinViewModel(),
    otherListViewModel: OtherListViewModel = koinViewModel(),
    lowVolumeWarningViewModel: LowVolumeWarningViewModel = koinViewModel(),
    backHandler: AppBackHandler = { _, _, _ -> },
    onDarkThemeChanged: (Boolean) -> Unit = {},
    autoReadoutContent: @Composable (scrollToTopRequest: Int) -> Unit = { scrollToTopRequest ->
        AutoReadoutContent(
            backHandler = backHandler,
            scrollToTopRequest = scrollToTopRequest,
            detailContent = { itemType -> ReadoutItemDetailContent(itemType) },
        )
    },
    manualReadoutContent: @Composable (selectedSimulatorId: String, tabReselectRequest: Int) -> Unit =
        { selectedSimulatorId, tabReselectRequest ->
            key(selectedSimulatorId) {
                ManualReadoutContent(
                    simulatorId = selectedSimulatorId,
                    tabReselectRequest = tabReselectRequest,
                    simulatorName = appScreenPrimarySimulatorLabel(selectedSimulatorId),
                    backHandler = backHandler,
                    detailContent = { setId, canNavigateBack, onBack ->
                        ManualReadoutSetDetailContent(
                            simulatorId = selectedSimulatorId,
                            setId = setId,
                            onDeleted = onBack,
                            canNavigateBack = canNavigateBack,
                            onBack = onBack,
                        )
                    },
                )
            }
        },
    speechRecognitionContent: @Composable (scrollToTopRequest: Int) -> Unit = { scrollToTopRequest ->
        SpeechRecognitionListPane(
            scrollToTopRequest = scrollToTopRequest,
            recognitionCard = { LocalSpeechRecognition.current.card(Modifier) },
        )
    },
    otherContent: @Composable (
        scrollToTopRequest: Int,
        selectedSimulatorId: String,
    ) -> Unit = { scrollToTopRequest, selectedSimulatorId ->
        DefaultOtherContent(
            selectedSimulatorId = selectedSimulatorId,
            backHandler = backHandler,
            scrollToTopRequest = scrollToTopRequest,
        )
    },
) {
    val darkTheme = rememberAppDarkTheme()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()
    val bannerUiState =
        rememberConnectionBannerUiState().withAccessLocalNetworkPermission(
            granted = uiState.accessLocalNetworkPermissionGranted,
            requiredMessage = stringResource(Res.string.banner_access_local_network_permission_required),
        )
    // 恒久拒否の判定状態を共有するため、画面を開いたときの要求とバナーのタップで同じ要求関数を使う
    val requestAccessLocalNetworkPermission =
        rememberAccessLocalNetworkPermissionRequester(onResult = viewModel::checkAccessLocalNetworkPermission)
    val requestAndOpenSettingsIfDenied =
        remember(requestAccessLocalNetworkPermission) { { requestAccessLocalNetworkPermission(true) } }
    val requestWithoutOpeningSettings =
        remember(requestAccessLocalNetworkPermission) { { requestAccessLocalNetworkPermission(false) } }
    val autoReadoutListUiState by autoReadoutListViewModel.uiState.collectAsState()
    val otherListUiState by otherListViewModel.uiState.collectAsStateWithLifecycle()
    val lowVolumeWarning by lowVolumeWarningViewModel.uiState.collectAsStateWithLifecycle()
    var manualReadoutTabReselectRequest by rememberSaveable { mutableIntStateOf(0) }
    var autoReadoutListScrollToTopRequest by rememberSaveable { mutableIntStateOf(0) }
    var otherListScrollToTopRequest by rememberSaveable { mutableIntStateOf(0) }
    var speechRecognitionScrollToTopRequest by rememberSaveable { mutableIntStateOf(0) }

    AppStartupEffects(
        darkTheme = darkTheme,
        checkUpdate = viewModel::checkUpdate,
        onDarkThemeChanged = onDarkThemeChanged,
    )

    ConnectionSnackbarEffect(
        isConnectionChecked = bannerUiState.isConnectionChecked,
        isConnected = bannerUiState.isConnected,
        snackbarHostState = snackbarHostState,
        connectedMessage = bannerUiState.snackbarConnectedMessage,
        disconnectedMessage = bannerUiState.snackbarDisconnectedMessage,
    )

    SimulatorChangeScrollToTopEffect(
        selectedSimulatorId = uiState.selectedSimulatorId,
        requestScrollToTop = { autoReadoutListScrollToTopRequest++ },
    )

    AppNarratorEffects(hapticFeedbackEnabled = uiState.hapticFeedbackEnabled)

    AppTtsAvailabilityEffect(otherListViewModel::checkTextToSpeechAvailability)
    val uriHandler = LocalUriHandler.current
    val openTtsSettings = rememberOpenTtsSettings()

    AccessLocalNetworkPermissionOnOpenEffect(
        selectedItem = otherListUiState.selectedItem,
        granted = uiState.accessLocalNetworkPermissionGranted,
        request = requestWithoutOpeningSettings,
    )

    LifecycleResumeEffect(Unit) {
        viewModel.checkAccessLocalNetworkPermission()
        onPauseOrDispose {}
    }

    CompositionLocalProvider(LocalSpeechRecognition provides rememberSpeechRecognition()) {
        AppScreenContent(
            darkTheme = darkTheme,
            dynamicColorEnabled = uiState.dynamicColorEnabled,
            bannerUiState = bannerUiState,
            ttsUnavailableGuidance = otherListUiState.ttsUnavailableGuidance,
            lowVolumeWarning = lowVolumeWarning,
            onLowVolumeBannerDismiss = lowVolumeWarningViewModel::dismiss,
            onLowVolumeBannerClick = { otherListViewModel.selectItem(OtherListItemType.Volume) },
            onTtsAction = {
                handleTtsUnavailableAction(
                    guidance = otherListUiState.ttsUnavailableGuidance,
                    openUri = uriHandler::openUri,
                    openTtsSettings = openTtsSettings,
                    openWindowsSpeechSettings = otherListViewModel::openWindowsSpeechSettings,
                )
            },
            snackbarHostState = snackbarHostState,
            hasAppUpdate = uiState.hasAppUpdate,
            accessLocalNetworkPermissionGranted = uiState.accessLocalNetworkPermissionGranted,
            keepScreenOn = uiState.keepScreenOn,
            hapticFeedbackEnabled = uiState.hapticFeedbackEnabled,
            selectedSimulatorId = uiState.selectedSimulatorId,
            onSimulatorSelected = viewModel::selectSimulator,
            onBannerTap =
                rememberConnectionBannerTap(
                    bannerUiState = bannerUiState,
                    onSelectOtherItem = otherListViewModel::selectItem,
                    onRequestAccessLocalNetworkPermission = requestAndOpenSettingsIfDenied,
                ),
            onManualReadoutTabReselected = { manualReadoutTabReselectRequest++ },
            manualReadoutTabReselectRequest = manualReadoutTabReselectRequest,
            onAutoReadoutTabReselected = {
                handleTabReselected(
                    selectedItem = autoReadoutListUiState.selectedItem,
                    clearSelectedItem = autoReadoutListViewModel::clearSelectedItem,
                    requestScrollToTop = { autoReadoutListScrollToTopRequest++ },
                )
            },
            onSpeechRecognitionTabReselected = { speechRecognitionScrollToTopRequest++ },
            onOtherTabReselected = {
                handleTabReselected(
                    selectedItem = otherListUiState.selectedItem,
                    clearSelectedItem = otherListViewModel::clearSelectedItem,
                    requestScrollToTop = { otherListScrollToTopRequest++ },
                )
            },
            autoReadoutContent = autoReadoutContent,
            autoReadoutListScrollToTopRequest = autoReadoutListScrollToTopRequest,
            otherListScrollToTopRequest = otherListScrollToTopRequest,
            speechRecognitionScrollToTopRequest = speechRecognitionScrollToTopRequest,
            manualReadoutContent = manualReadoutContent,
            speechRecognitionContent = speechRecognitionContent,
            otherContent = otherContent,
        )
    }
}

/**
 * シミュレーター選択が変更されたときに、自動読み上げタブのlistPaneを最上部までスクロールさせる。
 * ACE選択時の注意文言などlistPane先頭の項目が、スクロールしたままの状態だと
 * シミュレーター変更後も見えなくなるため、変更を検知して自動的に先頭へ戻す。
 * 初回コンポジション時には発火させない。
 */
@Composable
internal fun SimulatorChangeScrollToTopEffect(
    selectedSimulatorId: String,
    requestScrollToTop: () -> Unit,
) {
    val previousSimulatorId = remember { mutableStateOf<String?>(null) }
    val currentRequestScrollToTop = rememberUpdatedState(requestScrollToTop)
    SideEffect(selectedSimulatorId) {
        val prev = previousSimulatorId.value
        previousSimulatorId.value = selectedSimulatorId
        if (prev != null && prev != selectedSimulatorId) {
            currentRequestScrollToTop.value()
        }
    }
}

@Composable
internal fun ConnectionSnackbarEffect(
    isConnectionChecked: Boolean,
    isConnected: Boolean,
    snackbarHostState: SnackbarHostState,
    connectedMessage: String,
    disconnectedMessage: String,
) {
    val previousIsConnected = remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(isConnectionChecked, isConnected) {
        if (isConnectionChecked) {
            val prev = previousIsConnected.value
            previousIsConnected.value = isConnected
            if (prev != null && prev != isConnected) {
                snackbarHostState.showSnackbar(
                    message = if (isConnected) connectedMessage else disconnectedMessage,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }
}

@Composable
internal fun AppScreenContent(
    darkTheme: Boolean = false,
    dynamicColorEnabled: Boolean = false,
    layoutType: NavigationSuiteType? = null,
    bannerUiState: ConnectionBannerUiState = ConnectionBannerUiState(),
    ttsUnavailableGuidance: TtsUnavailableGuidance? = null,
    lowVolumeWarning: LowVolumeWarningUiState = LowVolumeWarningUiState(),
    onLowVolumeBannerClick: () -> Unit = {},
    onLowVolumeBannerDismiss: () -> Unit = {},
    onTtsAction: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    hasAppUpdate: Boolean = false,
    accessLocalNetworkPermissionGranted: Boolean = true,
    keepScreenOn: Boolean = false,
    hapticFeedbackEnabled: Boolean = true,
    selectedSimulatorId: String = LMU_WINDOWS_SIMULATOR_ID,
    onSimulatorSelected: (String) -> Unit = {},
    onBannerTap: (() -> Unit)? = null,
    onManualReadoutTabReselected: () -> Unit = {},
    manualReadoutTabReselectRequest: Int = 0,
    onAutoReadoutTabReselected: () -> Unit = {},
    onSpeechRecognitionTabReselected: () -> Unit = {},
    onOtherTabReselected: () -> Unit = {},
    autoReadoutContent: @Composable (scrollToTopRequest: Int) -> Unit = {},
    autoReadoutListScrollToTopRequest: Int = 0,
    manualReadoutContent: @Composable (selectedSimulatorId: String, tabReselectRequest: Int) -> Unit = { _, _ -> },
    speechRecognitionContent: @Composable (scrollToTopRequest: Int) -> Unit = {},
    otherContent: @Composable (scrollToTopRequest: Int, selectedSimulatorId: String) -> Unit = { _, _ -> },
    otherListScrollToTopRequest: Int = 0,
    speechRecognitionScrollToTopRequest: Int = 0,
) {
    val navigationState = rememberAppNavigationState()
    // 権限の要求はその場で行うため、その他タブへは切り替えない（IPアドレスの設定が必要な遷移のみ切り替える）
    val onBannerTapWithTabSwitch =
        if (bannerUiState.tapNavigationTarget == ConnectionBannerNavigationTarget.AccessLocalNetworkPermission) {
            onBannerTap
        } else {
            withTabSwitch(onBannerTap) {
                navigationState.navigateTo(AppDestination.More)
            }
        }
    val onLowVolumeBannerClickWithTabSwitch =
        {
            navigationState.navigateTo(AppDestination.More)
            onLowVolumeBannerClick()
        }
    val ambientHapticFeedback = LocalHapticFeedback.current
    val effectiveHapticFeedback =
        if (hapticFeedbackEnabled) ambientHapticFeedback else NoOpHapticFeedback

    CompositionLocalProvider(
        LocalHapticFeedback provides effectiveHapticFeedback,
        speechRecognitionChipOverlayInset,
    ) {
        AppTheme(darkTheme = darkTheme, dynamicColor = dynamicColorEnabled) {
            val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
            val resolvedLayoutType = layoutType ?: windowSizeClass.resolveNavigationSuiteType()
            KeepScreenOnEffect(keepScreenOn)
            AppScreenScaffold(
                resolvedLayoutType = resolvedLayoutType,
                hasAppUpdate = hasAppUpdate,
                accessLocalNetworkPermissionGranted = accessLocalNetworkPermissionGranted,
                selectedSimulatorId = selectedSimulatorId,
                onSimulatorSelected = onSimulatorSelected,
                bannerUiState = bannerUiState,
                ttsUnavailableGuidance = ttsUnavailableGuidance,
                lowVolumeWarning = lowVolumeWarning,
                onLowVolumeBannerClickWithTabSwitch = onLowVolumeBannerClickWithTabSwitch,
                onLowVolumeBannerDismiss = onLowVolumeBannerDismiss,
                onTtsAction = onTtsAction,
                snackbarHostState = snackbarHostState,
                navigationState = navigationState,
                onBannerTapWithTabSwitch = onBannerTapWithTabSwitch,
                onManualReadoutTabReselected = onManualReadoutTabReselected,
                manualReadoutTabReselectRequest = manualReadoutTabReselectRequest,
                onAutoReadoutTabReselected = onAutoReadoutTabReselected,
                onSpeechRecognitionTabReselected = onSpeechRecognitionTabReselected,
                onOtherTabReselected = onOtherTabReselected,
                autoReadoutContent = autoReadoutContent,
                autoReadoutListScrollToTopRequest = autoReadoutListScrollToTopRequest,
                manualReadoutContent = manualReadoutContent,
                speechRecognitionContent = speechRecognitionContent,
                otherContent = otherContent,
                otherListScrollToTopRequest = otherListScrollToTopRequest,
                speechRecognitionScrollToTopRequest = speechRecognitionScrollToTopRequest,
            )
        }
    }
}

@Composable
private fun AppScreenScaffold(
    resolvedLayoutType: NavigationSuiteType,
    hasAppUpdate: Boolean,
    accessLocalNetworkPermissionGranted: Boolean,
    selectedSimulatorId: String,
    onSimulatorSelected: (String) -> Unit,
    bannerUiState: ConnectionBannerUiState,
    ttsUnavailableGuidance: TtsUnavailableGuidance?,
    lowVolumeWarning: LowVolumeWarningUiState,
    onLowVolumeBannerClickWithTabSwitch: () -> Unit,
    onLowVolumeBannerDismiss: () -> Unit,
    onTtsAction: () -> Unit,
    snackbarHostState: SnackbarHostState,
    navigationState: AppNavigationState,
    onBannerTapWithTabSwitch: (() -> Unit)?,
    onManualReadoutTabReselected: () -> Unit,
    manualReadoutTabReselectRequest: Int,
    onAutoReadoutTabReselected: () -> Unit,
    onSpeechRecognitionTabReselected: () -> Unit,
    onOtherTabReselected: () -> Unit,
    autoReadoutContent: @Composable (scrollToTopRequest: Int) -> Unit,
    autoReadoutListScrollToTopRequest: Int,
    manualReadoutContent: @Composable (selectedSimulatorId: String, tabReselectRequest: Int) -> Unit,
    speechRecognitionContent: @Composable (scrollToTopRequest: Int) -> Unit,
    otherContent: @Composable (scrollToTopRequest: Int, selectedSimulatorId: String) -> Unit,
    otherListScrollToTopRequest: Int,
    speechRecognitionScrollToTopRequest: Int,
) {
    val haptic = LocalHapticFeedback.current
    var simulatorMenuExpanded by remember { mutableStateOf(false) }
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding(),
    ) {
        NavigationSuiteScaffold(
            navigationSuiteType = resolvedLayoutType,
            navigationItemVerticalArrangement = Arrangement.Center,
            navigationItems = {
                AppScreenPrimarySimulatorNavItem(
                    resolvedLayoutType = resolvedLayoutType,
                    selectedSimulatorId = selectedSimulatorId,
                    expanded = simulatorMenuExpanded,
                    onExpandedChange = { simulatorMenuExpanded = it },
                    onSimulatorSelected = onSimulatorSelected,
                )
                AppDestination.entries.forEach { dest ->
                    val showBadge =
                        dest == AppDestination.More &&
                            (hasAppUpdate || !accessLocalNetworkPermissionGranted)
                    NavigationSuiteItem(
                        icon = { AppNavIcon(dest = dest, showBadge = showBadge) },
                        label = { Text(dest.label()) },
                        selected = navigationState.current == dest,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            navigationState.handleTabClick(dest) { reselected ->
                                when (reselected) {
                                    AppDestination.AutoReadout -> onAutoReadoutTabReselected()
                                    AppDestination.ManualReadout -> onManualReadoutTabReselected()
                                    AppDestination.SpeechRecognition -> onSpeechRecognitionTabReselected()
                                    AppDestination.More -> onOtherTabReselected()
                                }
                            }
                        },
                        navigationSuiteType = resolvedLayoutType,
                    )
                }
            },
        ) {
            val dividerColor = DividerDefaults.color
            val dividerThickness = DividerDefaults.Thickness
            val contentModifier =
                Modifier
                    .fillMaxSize()
                    .then(
                        if (resolvedLayoutType == NavigationSuiteType.NavigationBar) {
                            Modifier
                        } else {
                            Modifier.drawWithContent {
                                drawContent()
                                val strokeWidth = dividerThickness.toPx()
                                drawLine(
                                    color = dividerColor,
                                    start = Offset(strokeWidth / 2, 0f),
                                    end = Offset(strokeWidth / 2, size.height),
                                    strokeWidth = strokeWidth,
                                )
                            }
                        },
                    )
            Column(modifier = contentModifier) {
                AnimatedVisibility(
                    visible = bannerUiState.isVisible,
                    enter = BannerEnterTransition,
                    exit = BannerExitTransition,
                ) {
                    ConnectionBannerContent(
                        uiState = bannerUiState,
                        onClick = onBannerTapWithTabSwitch,
                    )
                }
                WarningBanners(
                    isConnectionBannerVisible = bannerUiState.isVisible,
                    ttsUnavailableGuidance = ttsUnavailableGuidance,
                    onTtsAction = onTtsAction,
                    lowVolumeWarning = lowVolumeWarning,
                    onLowVolumeBannerClick = onLowVolumeBannerClickWithTabSwitch,
                    onLowVolumeBannerDismiss = onLowVolumeBannerDismiss,
                )
                AnimatedContent(
                    targetState = navigationState.current,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    modifier = Modifier.weight(1f),
                ) { destination ->
                    AppDestinationContent(
                        destination = destination,
                        autoReadoutContent = { autoReadoutContent(autoReadoutListScrollToTopRequest) },
                        manualReadoutContent = {
                            manualReadoutContent(selectedSimulatorId, manualReadoutTabReselectRequest)
                        },
                        speechRecognitionContent = { speechRecognitionContent(speechRecognitionScrollToTopRequest) },
                        otherContent = { otherContent(otherListScrollToTopRequest, selectedSimulatorId) },
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = AppSpacing.large,
                        // 右下のチップに隠れないよう、チップの幅だけ右端を空ける
                        end = AppSpacing.large + SPEECH_RECOGNITION_CHIP_OVERLAY_INSET,
                        bottom =
                            if (resolvedLayoutType == NavigationSuiteType.NavigationBar) {
                                NAVIGATION_BAR_OVERLAY_BOTTOM_OFFSET
                            } else {
                                AppSpacing.large
                            },
                    ),
        )
        LocalSpeechRecognition.current.chip(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = AppSpacing.large,
                    bottom =
                        if (resolvedLayoutType == NavigationSuiteType.NavigationBar) {
                            NAVIGATION_BAR_OVERLAY_BOTTOM_OFFSET
                        } else {
                            AppSpacing.large
                        },
                ),
        )
    }
}

internal fun WindowSizeClass.resolveNavigationSuiteType(): NavigationSuiteType =
    when {
        isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> {
            NavigationSuiteType.NavigationRail
        }

        else -> {
            NavigationSuiteType.NavigationBar
        }
    }

@Composable
internal fun ReadoutItemDetailContent(itemType: AutoReadoutListItemType) {
    when (itemType) {
        is LmuWindowsAutoReadoutListItemType -> LmuWindowsAutoReadoutItemDetailContent(itemType)
        is Gt7Ps5AutoReadoutListItemType -> Gt7Ps5AutoReadoutItemDetailContent(itemType)
        is AceWindowsAutoReadoutListItemType -> AceWindowsAutoReadoutItemDetailContent(itemType)
    }
}

@Preview(showBackground = true)
@Composable
private fun AppScreenContentPreview() {
    AppTheme {
        AppScreenContent()
    }
}
