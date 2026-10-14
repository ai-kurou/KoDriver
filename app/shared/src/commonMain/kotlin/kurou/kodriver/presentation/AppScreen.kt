package kurou.kodriver.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.app.shared.generated.resources.Res
import kurou.kodriver.app.shared.generated.resources.nav_auto_readout
import kurou.kodriver.app.shared.generated.resources.nav_log
import kurou.kodriver.app.shared.generated.resources.nav_manual_readout
import kurou.kodriver.app.shared.generated.resources.nav_more
import kurou.kodriver.feature.acewindowsdebugstatedetail.AceWindowsDebugStateDetailPane
import kurou.kodriver.feature.autoreadoutlist.AceWindowsAutoReadoutListItemType
import kurou.kodriver.feature.autoreadoutlist.AutoReadoutContent
import kurou.kodriver.feature.autoreadoutlist.AutoReadoutListItemType
import kurou.kodriver.feature.autoreadoutlist.AutoReadoutListViewModel
import kurou.kodriver.feature.autoreadoutlist.Gt7Ps5AutoReadoutListItemType
import kurou.kodriver.feature.autoreadoutlist.LmuWindowsAutoReadoutListItemType
import kurou.kodriver.feature.gt7ps5debugstatedetail.Gt7Ps5DebugStateDetailPane
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.LmuWindowsDebugStateDetailPane
import kurou.kodriver.feature.main.AppScreenPrimarySimulatorIndicator
import kurou.kodriver.feature.main.AppScreenViewModel
import kurou.kodriver.feature.main.appScreenPrimarySimulatorLabel
import kurou.kodriver.feature.manualreadoutlist.ManualReadoutContent
import kurou.kodriver.feature.manualreadoutsetdetail.ManualReadoutSetDetailContent
import kurou.kodriver.feature.otherconsoleipdetail.OtherConsoleIpDetailPane
import kurou.kodriver.feature.otherfeedbackdetail.OtherFeedbackDetailPane
import kurou.kodriver.feature.otherlicensedetail.OtherLicenseDetailPane
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListViewModel
import kurou.kodriver.feature.otherlist.TtsUnavailableGuidance
import kurou.kodriver.feature.otherlist.rememberOpenTtsSettings
import kurou.kodriver.feature.otheroverlaybackgroundopacitydetail.OtherOverlayBackgroundOpacityDetailPane
import kurou.kodriver.feature.otheroverlaytextcolordetail.OtherOverlayTextColorDetailDialog
import kurou.kodriver.feature.otheroverlaytextsizedetail.OtherOverlayTextSizeDetailDialog
import kurou.kodriver.feature.otherreadoutstartsounddetail.OtherReadoutStartSoundDetailDialog
import kurou.kodriver.feature.otherserveripdetail.OtherServerIpDetailPane
import kurou.kodriver.feature.otherthemedetail.OtherThemeDetailDialog
import kurou.kodriver.feature.telemetrylogdetail.TelemetryLogDetailContent
import kurou.kodriver.feature.telemetryloglist.TelemetryLogContent
import kurou.kodriver.feature.telemetryloglist.TelemetryLogListViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * `:core:domain` の `Simulator.LmuWindows.id` と一致させる、初期選択シミュレーターの ID。
 * app:shared は `:core:domain` に依存しないため、Preview / デフォルト値用にリテラルで保持する。
 */
private const val LMU_WINDOWS_SIMULATOR_ID = "lmu_windows"
private const val GT7_PS5_SIMULATOR_ID = "gt7_ps5"
private const val ACE_WINDOWS_SIMULATOR_ID = "ace_windows"

internal enum class DebugStateDetailType { LMU_WINDOWS, GT7_PS5, ACE_WINDOWS, UNKNOWN }

internal fun debugStateDetailType(selectedSimulatorId: String): DebugStateDetailType =
    when (selectedSimulatorId) {
        LMU_WINDOWS_SIMULATOR_ID -> DebugStateDetailType.LMU_WINDOWS
        GT7_PS5_SIMULATOR_ID -> DebugStateDetailType.GT7_PS5
        ACE_WINDOWS_SIMULATOR_ID -> DebugStateDetailType.ACE_WINDOWS
        else -> DebugStateDetailType.UNKNOWN
    }

@Composable
internal fun DebugStateDetailPane(
    selectedSimulatorId: String,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    lmuWindowsPane: @Composable (canNavigateBack: Boolean, onBack: () -> Unit) -> Unit = { canBack, back ->
        LmuWindowsDebugStateDetailPane(canBack, back)
    },
    gt7Ps5Pane: @Composable (canNavigateBack: Boolean, onBack: () -> Unit) -> Unit = { canBack, back ->
        Gt7Ps5DebugStateDetailPane(canBack, back)
    },
    aceWindowsPane: @Composable (canNavigateBack: Boolean, onBack: () -> Unit) -> Unit = { canBack, back ->
        AceWindowsDebugStateDetailPane(canBack, back)
    },
) {
    when (debugStateDetailType(selectedSimulatorId)) {
        DebugStateDetailType.LMU_WINDOWS -> lmuWindowsPane(canNavigateBack, onBack)
        DebugStateDetailType.GT7_PS5 -> gt7Ps5Pane(canNavigateBack, onBack)
        DebugStateDetailType.ACE_WINDOWS -> aceWindowsPane(canNavigateBack, onBack)
        DebugStateDetailType.UNKNOWN -> Unit
    }
}

/**
 * シミュレーター選択項目のドロップダウン矢印アイコンを、メニュー展開時に回転させる角度。
 */
private const val HALF_ROTATION_DEGREES = 180f

/**
 * NavigationRail ヘッダーに表示するシミュレーターロゴのサイズ。
 * ナビ項目内のアイコン（24dp）より大きくし、ヘッダーであることを視覚的に強調する。
 */
private val PrimarySimulatorRailHeaderIconSize = 40.dp

/**
 * NavigationRail ヘッダーとナビ項目を区切る [HorizontalDivider] の幅。
 */
private val PrimarySimulatorRailHeaderDividerWidth = 32.dp

// 接続バナーとTTS警告バナーの間に背景色の隙間として見せる1dp。
private val ConnectionTtsBannerGap = 1.dp

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

private fun <T> withTabSwitchWithArg(
    action: ((T) -> Unit)?,
    switchToMore: () -> Unit,
): ((T) -> Unit)? =
    if (action != null) {
        { arg ->
            switchToMore()
            action(arg)
        }
    } else {
        null
    }

internal fun ConnectionBannerNavigationTarget.toOtherListItemType(): OtherListItemType =
    when (this) {
        ConnectionBannerNavigationTarget.ConsoleIp -> OtherListItemType.ConsoleIp
        ConnectionBannerNavigationTarget.ServerIp -> OtherListItemType.ServerIp
    }

@Composable
private fun AppDestinationContent(
    destination: AppDestination,
    autoReadoutContent: @Composable () -> Unit,
    telemetryLogContent: @Composable () -> Unit,
    manualReadoutContent: @Composable () -> Unit,
    otherContent: @Composable () -> Unit,
) {
    when (destination) {
        AppDestination.AutoReadout -> autoReadoutContent()
        AppDestination.ManualReadout -> manualReadoutContent()
        AppDestination.Log -> telemetryLogContent()
        AppDestination.More -> otherContent()
    }
}

@Composable
private fun AppDestination.label(): String =
    when (this) {
        AppDestination.AutoReadout -> stringResource(Res.string.nav_auto_readout)
        AppDestination.ManualReadout -> stringResource(Res.string.nav_manual_readout)
        AppDestination.Log -> stringResource(Res.string.nav_log)
        AppDestination.More -> stringResource(Res.string.nav_more)
    }

@Composable
private fun AppNavIcon(
    dest: AppDestination,
    showBadge: Boolean,
    modifier: Modifier = Modifier,
) {
    BadgedBox(
        badge = {
            AnimatedVisibility(
                visible = showBadge,
                enter =
                    scaleIn(animationSpec = tween(durationMillis = 300)) +
                        fadeIn(animationSpec = tween(durationMillis = 300)),
                exit =
                    scaleOut(animationSpec = tween(durationMillis = 200)) +
                        fadeOut(animationSpec = tween(durationMillis = 200)),
            ) {
                Badge()
            }
        },
    ) {
        Icon(dest.icon, contentDescription = dest.label(), modifier = modifier)
    }
}

/**
 * NavigationBar / NavigationRail の先頭に表示する、現在選択中のシミュレーターの項目。
 * 他の [AppDestination] とは異なりタブ切り替えの対象ではないため、常に非選択として扱う。
 * タップするとシミュレーター選択メニューを開く。ラベル横にドロップダウン矢印アイコンを添えて、
 * 単なるタブではなくメニューを開く操作対象であることを視覚的に示す。NavigationBar ではメニューが
 * 上方向に展開されるため矢印を上向き（[Icons.Filled.ArrowDropUp]）にし、NavigationRail では
 * 右方向に展開されるため矢印を右向き（[Icons.AutoMirrored.Filled.ArrowRight]）にする。
 * ポップアップメニューの開閉状態（[expanded]）に応じて矢印アイコンを180度回転させ、
 * 開いているときは向きが反対になり、閉じると元の向きに戻ることでメニューの開閉状態を視覚的に示す。
 *
 * NavigationRail では、通常のナビ項目より大きくロゴを表示したうえで [HorizontalDivider] を添え、
 * シミュレーター選択（ヘッダ）とタブ切り替え（ナビ項目）が別の操作であることを視覚的に示すヘッダーとして表示する。
 */
@Composable
private fun AppScreenPrimarySimulatorNavItem(
    resolvedLayoutType: NavigationSuiteType,
    selectedSimulatorId: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSimulatorSelected: (String) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    if (resolvedLayoutType == NavigationSuiteType.NavigationBar) {
        NavigationSuiteItem(
            icon = {
                AppScreenPrimarySimulatorIndicator(
                    simulatorId = selectedSimulatorId,
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    onSimulatorSelected = onSimulatorSelected,
                    modifier = Modifier.size(24.dp),
                )
            },
            label = {
                val dropdownArrowRotation by
                    animateFloatAsState(targetValue = if (expanded) HALF_ROTATION_DEGREES else 0f)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(appScreenPrimarySimulatorLabel(selectedSimulatorId))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropUp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp).rotate(dropdownArrowRotation),
                    )
                }
            },
            selected = false,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                onExpandedChange(true)
            },
            navigationSuiteType = resolvedLayoutType,
            modifier = Modifier.testTag("primarySimulatorNavItem"),
        )
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .padding(vertical = AppSpacing.medium)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onExpandedChange(true)
                    }.testTag("primarySimulatorNavItem"),
        ) {
            AppScreenPrimarySimulatorIndicator(
                simulatorId = selectedSimulatorId,
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                onSimulatorSelected = onSimulatorSelected,
                modifier = Modifier.size(PrimarySimulatorRailHeaderIconSize),
            )
            Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
            val dropdownArrowRotation by animateFloatAsState(targetValue = if (expanded) HALF_ROTATION_DEGREES else 0f)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(appScreenPrimarySimulatorLabel(selectedSimulatorId), style = MaterialTheme.typography.labelMedium)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).rotate(dropdownArrowRotation),
                )
            }
            Spacer(modifier = Modifier.height(AppSpacing.medium))
            HorizontalDivider(modifier = Modifier.width(PrimarySimulatorRailHeaderDividerWidth))
        }
    }
}

@Composable
private fun DefaultOtherContent(
    selectedSimulatorId: String,
    backHandler: AppBackHandler,
    scrollToTopRequest: Int,
) {
    var showReadoutStartSoundDialog by rememberSaveable { mutableStateOf(false) }
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showOverlayTextColorDialog by rememberSaveable { mutableStateOf(false) }
    var showOverlayTextSizeDialog by rememberSaveable { mutableStateOf(false) }
    if (showReadoutStartSoundDialog) {
        OtherReadoutStartSoundDetailDialog(
            simulatorId = selectedSimulatorId,
            onDismiss = { showReadoutStartSoundDialog = false },
        )
    }
    if (showThemeDialog) {
        OtherThemeDetailDialog(onDismiss = { showThemeDialog = false })
    }
    if (showOverlayTextColorDialog) {
        OtherOverlayTextColorDetailDialog(onDismiss = { showOverlayTextColorDialog = false })
    }
    if (showOverlayTextSizeDialog) {
        OtherOverlayTextSizeDetailDialog(onDismiss = { showOverlayTextSizeDialog = false })
    }
    OtherContent(
        backHandler = backHandler,
        scrollToTopRequest = scrollToTopRequest,
        onOpenReadoutStartSoundDialog = { showReadoutStartSoundDialog = true },
        onOpenThemeDialog = { showThemeDialog = true },
        onOpenOverlayTextColorDialog = { showOverlayTextColorDialog = true },
        onOpenOverlayTextSizeDialog = { showOverlayTextSizeDialog = true },
        detailContent = { itemType, canNavigateBack, onBack, feedbackTelemetryLogId, feedbackAttachRequestId ->
            when (itemType) {
                OtherListItemType.ServerIp -> {
                    OtherServerIpDetailPane(canNavigateBack, onBack)
                }

                OtherListItemType.ConsoleIp -> {
                    OtherConsoleIpDetailPane(canNavigateBack, onBack)
                }

                OtherListItemType.Volume,
                OtherListItemType.Voice,
                OtherListItemType.VoiceSpeed,
                OtherListItemType.VoicePitch,
                -> {
                    OtherReadoutSettingsDetailPane(itemType, canNavigateBack, onBack)
                }

                OtherListItemType.OverlayBackgroundOpacity -> {
                    OtherOverlayBackgroundOpacityDetailPane(canNavigateBack, onBack)
                }

                OtherListItemType.License -> {
                    OtherLicenseDetailPane(canNavigateBack, onBack)
                }

                OtherListItemType.Feedback -> {
                    OtherFeedbackDetailPane(
                        canNavigateBack,
                        onBack,
                        telemetryLogId = feedbackTelemetryLogId,
                        telemetryLogAttachRequestId = feedbackAttachRequestId,
                    )
                }

                OtherListItemType.DebugState -> {
                    DebugStateDetailPane(selectedSimulatorId, canNavigateBack, onBack)
                }

                OtherListItemType.OverlayTextColor,
                OtherListItemType.OverlayVisible,
                OtherListItemType.KeepScreenOn,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.Theme,
                OtherListItemType.OverlayTextSize,
                OtherListItemType.DynamicColor,
                OtherListItemType.HapticFeedback,
                OtherListItemType.Startup,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.AccessLocalNetworkPermission,
                OtherListItemType.TtsEngineMissing,
                OtherListItemType.TtsLanguageDataMissing,
                OtherListItemType.WindowsSpeechUnavailable,
                -> {}
            }
        },
    )
}

/**
 * AppScreen を提供する公開関数。
 */
@Composable
fun AppScreen(
    viewModel: AppScreenViewModel = koinViewModel(),
    autoReadoutListViewModel: AutoReadoutListViewModel = koinViewModel(),
    telemetryLogListViewModel: TelemetryLogListViewModel = koinViewModel(),
    otherListViewModel: OtherListViewModel = koinViewModel(),
    backHandler: AppBackHandler = { _, _, _ -> },
    onDarkThemeChanged: (Boolean) -> Unit = {},
    autoReadoutContent: @Composable (scrollToTopRequest: Int) -> Unit = { scrollToTopRequest ->
        AutoReadoutContent(
            backHandler = backHandler,
            scrollToTopRequest = scrollToTopRequest,
            detailContent = { itemType -> ReadoutItemDetailContent(itemType) },
        )
    },
    telemetryLogContent: @Composable (scrollToTopRequest: Int, onFeedbackClick: (Long) -> Unit) -> Unit = {
        scrollToTopRequest,
        onFeedbackClick,
        ->
        TelemetryLogContent(
            backHandler = backHandler,
            scrollToTopRequest = scrollToTopRequest,
            onFeedbackClick = onFeedbackClick,
            detailContent = { id ->
                TelemetryLogDetailContent(id = id)
            },
        )
    },
    manualReadoutContent: @Composable (selectedSimulatorId: String) -> Unit = { selectedSimulatorId ->
        key(selectedSimulatorId) {
            ManualReadoutContent(
                simulatorId = selectedSimulatorId,
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
    val bannerUiState = rememberConnectionBannerUiState()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()
    val autoReadoutListUiState by autoReadoutListViewModel.uiState.collectAsState()
    val telemetryLogListUiState by telemetryLogListViewModel.uiState.collectAsState()
    val otherListUiState by otherListViewModel.uiState.collectAsStateWithLifecycle()
    var autoReadoutListScrollToTopRequest by rememberSaveable { mutableIntStateOf(0) }
    var telemetryLogListScrollToTopRequest by rememberSaveable { mutableIntStateOf(0) }
    var otherListScrollToTopRequest by rememberSaveable { mutableIntStateOf(0) }

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

    AppNarratorEffects()

    AppTtsAvailabilityEffect(otherListViewModel::checkTextToSpeechAvailability)
    val uriHandler = LocalUriHandler.current
    val openTtsSettings = rememberOpenTtsSettings()

    LifecycleResumeEffect(Unit) {
        viewModel.checkAccessLocalNetworkPermission()
        onPauseOrDispose {}
    }

    AppScreenContent(
        darkTheme = darkTheme,
        dynamicColorEnabled = uiState.dynamicColorEnabled,
        bannerUiState = bannerUiState,
        ttsUnavailableGuidance = otherListUiState.ttsUnavailableGuidance,
        onTtsAction = {
            when (otherListUiState.ttsUnavailableGuidance) {
                TtsUnavailableGuidance.EngineMissing -> uriHandler.openUri(TTS_ENGINE_PLAY_STORE_URL)
                TtsUnavailableGuidance.LanguageDataMissing -> openTtsSettings()
                TtsUnavailableGuidance.WindowsSpeechUnavailable -> otherListViewModel.openWindowsSpeechSettings()
                null -> Unit
            }
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
            ),
        onFeedbackClick = { telemetryLogId -> otherListViewModel.selectFeedbackItem(telemetryLogId) },
        onAutoReadoutTabReselected = {
            handleTabReselected(
                selectedItem = autoReadoutListUiState.selectedItem,
                clearSelectedItem = autoReadoutListViewModel::clearSelectedItem,
                requestScrollToTop = { autoReadoutListScrollToTopRequest++ },
            )
        },
        onLogTabReselected = {
            handleTabReselected(
                selectedItem = telemetryLogListUiState.selectedLogId,
                clearSelectedItem = telemetryLogListViewModel::clearSelectedLog,
                requestScrollToTop = { telemetryLogListScrollToTopRequest++ },
            )
        },
        onOtherTabReselected = {
            handleTabReselected(
                selectedItem = otherListUiState.selectedItem,
                clearSelectedItem = otherListViewModel::clearSelectedItem,
                requestScrollToTop = { otherListScrollToTopRequest++ },
            )
        },
        autoReadoutContent = autoReadoutContent,
        autoReadoutListScrollToTopRequest = autoReadoutListScrollToTopRequest,
        telemetryLogListScrollToTopRequest = telemetryLogListScrollToTopRequest,
        otherListScrollToTopRequest = otherListScrollToTopRequest,
        telemetryLogContent = telemetryLogContent,
        manualReadoutContent = manualReadoutContent,
        otherContent = otherContent,
    )
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
    LaunchedEffect(selectedSimulatorId) {
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
    onTtsAction: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    hasAppUpdate: Boolean = false,
    accessLocalNetworkPermissionGranted: Boolean = true,
    keepScreenOn: Boolean = false,
    hapticFeedbackEnabled: Boolean = true,
    selectedSimulatorId: String = LMU_WINDOWS_SIMULATOR_ID,
    onSimulatorSelected: (String) -> Unit = {},
    onBannerTap: (() -> Unit)? = null,
    onFeedbackClick: ((Long) -> Unit)? = null,
    onAutoReadoutTabReselected: () -> Unit = {},
    onLogTabReselected: () -> Unit = {},
    onOtherTabReselected: () -> Unit = {},
    autoReadoutContent: @Composable (scrollToTopRequest: Int) -> Unit = {},
    autoReadoutListScrollToTopRequest: Int = 0,
    telemetryLogContent: @Composable (scrollToTopRequest: Int, onFeedbackClick: (Long) -> Unit) -> Unit = { _, _ -> },
    telemetryLogListScrollToTopRequest: Int = 0,
    manualReadoutContent: @Composable (selectedSimulatorId: String) -> Unit = {},
    otherContent: @Composable (scrollToTopRequest: Int, selectedSimulatorId: String) -> Unit = { _, _ -> },
    otherListScrollToTopRequest: Int = 0,
) {
    val navigationState = rememberAppNavigationState()
    val onBannerTapWithTabSwitch =
        withTabSwitch(onBannerTap) {
            navigationState.navigateTo(AppDestination.More)
        }
    val onFeedbackClickWithTabSwitch =
        withTabSwitchWithArg(onFeedbackClick) {
            navigationState.navigateTo(AppDestination.More)
        }
    val ambientHapticFeedback = LocalHapticFeedback.current
    val effectiveHapticFeedback =
        if (hapticFeedbackEnabled) ambientHapticFeedback else NoOpHapticFeedback

    CompositionLocalProvider(LocalHapticFeedback provides effectiveHapticFeedback) {
        AppTheme(darkTheme = darkTheme, dynamicColor = dynamicColorEnabled) {
            val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
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
                onTtsAction = onTtsAction,
                snackbarHostState = snackbarHostState,
                navigationState = navigationState,
                onBannerTapWithTabSwitch = onBannerTapWithTabSwitch,
                onFeedbackClickWithTabSwitch = onFeedbackClickWithTabSwitch,
                onAutoReadoutTabReselected = onAutoReadoutTabReselected,
                onLogTabReselected = onLogTabReselected,
                onOtherTabReselected = onOtherTabReselected,
                autoReadoutContent = autoReadoutContent,
                autoReadoutListScrollToTopRequest = autoReadoutListScrollToTopRequest,
                telemetryLogContent = telemetryLogContent,
                telemetryLogListScrollToTopRequest = telemetryLogListScrollToTopRequest,
                manualReadoutContent = manualReadoutContent,
                otherContent = otherContent,
                otherListScrollToTopRequest = otherListScrollToTopRequest,
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
    onTtsAction: () -> Unit,
    snackbarHostState: SnackbarHostState,
    navigationState: AppNavigationState,
    onBannerTapWithTabSwitch: (() -> Unit)?,
    onFeedbackClickWithTabSwitch: ((Long) -> Unit)?,
    onAutoReadoutTabReselected: () -> Unit,
    onLogTabReselected: () -> Unit,
    onOtherTabReselected: () -> Unit,
    autoReadoutContent: @Composable (scrollToTopRequest: Int) -> Unit,
    autoReadoutListScrollToTopRequest: Int,
    telemetryLogContent: @Composable (scrollToTopRequest: Int, onFeedbackClick: (Long) -> Unit) -> Unit,
    telemetryLogListScrollToTopRequest: Int,
    manualReadoutContent: @Composable (selectedSimulatorId: String) -> Unit,
    otherContent: @Composable (scrollToTopRequest: Int, selectedSimulatorId: String) -> Unit,
    otherListScrollToTopRequest: Int,
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
            modifier = Modifier.padding(top = AppSpacing.extraSmall),
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
                                    AppDestination.ManualReadout -> Unit
                                    AppDestination.Log -> onLogTabReselected()
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
                    enter =
                        slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(durationMillis = 300),
                        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit =
                        slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = tween(durationMillis = 200),
                        ) + fadeOut(animationSpec = tween(durationMillis = 200)),
                ) {
                    ConnectionBannerContent(
                        uiState = bannerUiState,
                        onClick = onBannerTapWithTabSwitch,
                    )
                }
                if (bannerUiState.isVisible && ttsUnavailableGuidance != null) {
                    Spacer(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(ConnectionTtsBannerGap)
                                .background(MaterialTheme.colorScheme.background)
                                .testTag("connectionTtsBannerGap"),
                    )
                }
                TtsUnavailableBannerHost(reason = ttsUnavailableGuidance, onAction = onTtsAction)
                AnimatedContent(
                    targetState = navigationState.current,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    modifier = Modifier.weight(1f),
                ) { destination ->
                    AppDestinationContent(
                        destination = destination,
                        autoReadoutContent = { autoReadoutContent(autoReadoutListScrollToTopRequest) },
                        telemetryLogContent = {
                            telemetryLogContent(
                                telemetryLogListScrollToTopRequest,
                                onFeedbackClickWithTabSwitch ?: {},
                            )
                        },
                        manualReadoutContent = { manualReadoutContent(selectedSimulatorId) },
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
                        end = AppSpacing.large,
                        bottom =
                            if (resolvedLayoutType == NavigationSuiteType.NavigationBar) {
                                96.dp
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
