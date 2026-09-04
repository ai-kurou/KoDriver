package kurou.kodriver.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.app.shared.generated.resources.Res
import kurou.kodriver.app.shared.generated.resources.nav_log
import kurou.kodriver.app.shared.generated.resources.nav_more
import kurou.kodriver.app.shared.generated.resources.nav_readout
import kurou.kodriver.feature.acewindowsreadout.flagdetail.AceWindowsReadoutFlagDetailPane
import kurou.kodriver.feature.acewindowsreadout.mybestlapdetail.AceWindowsReadoutMyBestLapDetailPane
import kurou.kodriver.feature.acewindowsreadout.remainingfueldetail.AceWindowsReadoutRemainingFuelDetailPane
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.AceWindowsReadoutTyreTemperatureDetailPane
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.AceWindowsReadoutVehicleApproachDetailPane
import kurou.kodriver.feature.debugstatedetail.DebugStateDetailPane
import kurou.kodriver.feature.gt7ps5readout.mybestlapdetail.Gt7Ps5ReadoutMyBestLapDetailPane
import kurou.kodriver.feature.gt7ps5readout.remainingfueldetail.Gt7Ps5ReadoutRemainingFuelDetailPane
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.Gt7Ps5ReadoutRemainingFuelLapsDetailPane
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.Gt7Ps5ReadoutTyreTemperatureDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.LmuWindowsReadoutFlagDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.LmuWindowsReadoutMyBestLapDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.LmuWindowsReadoutPitTimingDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.LmuWindowsReadoutRemainingVirtualEnergyDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.LmuWindowsReadoutTyreTemperatureDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail.LmuWindowsReadoutTyreWearDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.LmuWindowsReadoutVehicleApproachDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.LmuWindowsReadoutVehicleDamageDetailPane
import kurou.kodriver.feature.main.AppScreenPrimarySimulatorIndicator
import kurou.kodriver.feature.main.AppScreenViewModel
import kurou.kodriver.feature.main.appScreenPrimarySimulatorLabel
import kurou.kodriver.feature.otherconsoleipdetail.OtherConsoleIpDetailPane
import kurou.kodriver.feature.otherfeedbackdetail.OtherFeedbackDetailPane
import kurou.kodriver.feature.otherlicensedetail.OtherLicenseDetailPane
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListViewModel
import kurou.kodriver.feature.otherreadoutstartsounddetail.OtherReadoutStartSoundDetailDialog
import kurou.kodriver.feature.otherserveripdetail.OtherServerIpDetailPane
import kurou.kodriver.feature.otherthemedetail.OtherThemeDetailDialog
import kurou.kodriver.feature.othervolumedetail.OtherVolumeDetailPane
import kurou.kodriver.feature.readoutlist.ReadoutContent
import kurou.kodriver.feature.readoutlist.ReadoutListItemType
import kurou.kodriver.feature.readoutlist.ReadoutListViewModel
import kurou.kodriver.feature.telemetrylogdetail.TelemetryLogDetailContent
import kurou.kodriver.feature.telemetryloglist.TelemetryLogContent
import kurou.kodriver.feature.telemetryloglist.TelemetryLogListViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * `:core:domain` の `Simulator.LmuWindows.id` と一致させる、初期選択シミュレータの ID。
 * app:shared は `:core:domain` に依存しないため、Preview / デフォルト値用にリテラルで保持する。
 */
private const val LMU_WINDOWS_SIMULATOR_ID = "lmu_windows"

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
    readoutContent: @Composable () -> Unit,
    telemetryLogContent: @Composable () -> Unit,
    otherContent: @Composable () -> Unit,
) {
    when (destination) {
        AppDestination.Readout -> readoutContent()
        AppDestination.Log -> telemetryLogContent()
        AppDestination.More -> otherContent()
    }
}

@Composable
private fun AppDestination.label(): String =
    when (this) {
        AppDestination.Readout -> stringResource(Res.string.nav_readout)
        AppDestination.Log -> stringResource(Res.string.nav_log)
        AppDestination.More -> stringResource(Res.string.nav_more)
    }

@Composable
private fun AppNavIcon(
    dest: AppDestination,
    showBadge: Boolean,
    modifier: Modifier = Modifier,
) {
    BadgedBox(badge = { if (showBadge) Badge() }) {
        Icon(dest.icon, contentDescription = dest.label(), modifier = modifier)
    }
}

/**
 * NavigationRail / NavigationBar の先頭に表示する、現在選択中のシミュレータの項目。
 * 他の [AppDestination] とは異なりタブ切り替えの対象ではないため、常に非選択（[selected] = false）とする。
 * タップするとシミュレータ選択メニューを開く。ラベル横にドロップダウン矢印アイコンを添えて、
 * 単なるタブではなくメニューを開く操作対象であることを視覚的に示す。画面下部の NavigationBar では
 * メニューが上方向に展開されるため矢印を上向き（[Icons.Filled.ArrowDropUp]）にし、画面横の
 * NavigationRail / NavigationDrawer では下向き（[Icons.Filled.ArrowDropDown]）のままとする。
 * NavigationDrawer ではラベルの横幅に余裕があるため、[MaterialTheme.typography.titleMedium] を使って
 * 他のタブラベルより強調表示する。
 */
private fun NavigationSuiteScope.appScreenPrimarySimulatorNavItem(
    resolvedLayoutType: NavigationSuiteType,
    selectedSimulatorId: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSimulatorSelected: (String) -> Unit,
) {
    val itemModifier =
        if (resolvedLayoutType == NavigationSuiteType.NavigationDrawer) {
            Modifier
                .fillMaxWidth()
                .padding(4.dp)
        } else {
            Modifier
        }
    val dropdownArrowIcon =
        if (resolvedLayoutType == NavigationSuiteType.NavigationBar) {
            Icons.Filled.ArrowDropUp
        } else {
            Icons.Filled.ArrowDropDown
        }
    item(
        icon = {
            if (resolvedLayoutType != NavigationSuiteType.NavigationDrawer) {
                AppScreenPrimarySimulatorIndicator(
                    simulatorId = selectedSimulatorId,
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    onSimulatorSelected = onSimulatorSelected,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        label = {
            if (resolvedLayoutType == NavigationSuiteType.NavigationDrawer) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .offset(x = (-6).dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppScreenPrimarySimulatorIndicator(
                        simulatorId = selectedSimulatorId,
                        expanded = expanded,
                        onExpandedChange = onExpandedChange,
                        onSimulatorSelected = onSimulatorSelected,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = appScreenPrimarySimulatorLabel(selectedSimulatorId),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Icon(
                        imageVector = dropdownArrowIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(appScreenPrimarySimulatorLabel(selectedSimulatorId))
                    Icon(
                        imageVector = dropdownArrowIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        selected = false,
        onClick = { onExpandedChange(true) },
        modifier = itemModifier.testTag("primarySimulatorNavItem"),
    )
}

@Composable
private fun DefaultOtherContent(
    backHandler: AppBackHandler,
    scrollToTopRequest: Int,
) {
    var showReadoutStartSoundDialog by rememberSaveable { mutableStateOf(false) }
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    if (showReadoutStartSoundDialog) {
        OtherReadoutStartSoundDetailDialog(onDismiss = { showReadoutStartSoundDialog = false })
    }
    if (showThemeDialog) {
        OtherThemeDetailDialog(onDismiss = { showThemeDialog = false })
    }
    OtherContent(
        backHandler = backHandler,
        scrollToTopRequest = scrollToTopRequest,
        onOpenReadoutStartSoundDialog = { showReadoutStartSoundDialog = true },
        onOpenThemeDialog = { showThemeDialog = true },
        detailContent = { itemType, canNavigateBack, onBack, feedbackTelemetryLogId, feedbackAttachRequestId ->
            when (itemType) {
                OtherListItemType.ServerIp -> {
                    OtherServerIpDetailPane(canNavigateBack, onBack)
                }

                OtherListItemType.ConsoleIp -> {
                    OtherConsoleIpDetailPane(canNavigateBack, onBack)
                }

                OtherListItemType.Volume -> {
                    OtherVolumeDetailPane(canNavigateBack, onBack)
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
                    DebugStateDetailPane(canNavigateBack, onBack)
                }

                OtherListItemType.KeepScreenOn,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.Theme,
                OtherListItemType.DynamicColor,
                OtherListItemType.HapticFeedback,
                OtherListItemType.Startup,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.AccessLocalNetworkPermission,
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
    readoutListViewModel: ReadoutListViewModel = koinViewModel(),
    telemetryLogListViewModel: TelemetryLogListViewModel = koinViewModel(),
    otherListViewModel: OtherListViewModel = koinViewModel(),
    backHandler: AppBackHandler = { _, _, _ -> },
    onDarkThemeChanged: (Boolean) -> Unit = {},
    readoutContent: @Composable (scrollToTopRequest: Int) -> Unit = { scrollToTopRequest ->
        ReadoutContent(
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
    otherContent: @Composable (scrollToTopRequest: Int) -> Unit = { scrollToTopRequest ->
        DefaultOtherContent(
            backHandler = backHandler,
            scrollToTopRequest = scrollToTopRequest,
        )
    },
) {
    val darkTheme = rememberAppDarkTheme()
    val bannerUiState = rememberConnectionBannerUiState()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()
    val readoutListUiState by readoutListViewModel.uiState.collectAsState()
    val telemetryLogListUiState by telemetryLogListViewModel.uiState.collectAsState()
    val otherListUiState by otherListViewModel.uiState.collectAsState()
    var readoutListScrollToTopRequest by rememberSaveable { mutableIntStateOf(0) }
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

    AppNarratorEffects()

    LifecycleResumeEffect(Unit) {
        viewModel.checkAccessLocalNetworkPermission()
        onPauseOrDispose {}
    }

    AppScreenContent(
        darkTheme = darkTheme,
        dynamicColorEnabled = uiState.dynamicColorEnabled,
        bannerUiState = bannerUiState,
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
        onReadoutTabReselected = {
            handleTabReselected(
                selectedItem = readoutListUiState.selectedItem,
                clearSelectedItem = readoutListViewModel::clearSelectedItem,
                requestScrollToTop = { readoutListScrollToTopRequest++ },
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
        readoutContent = readoutContent,
        readoutListScrollToTopRequest = readoutListScrollToTopRequest,
        telemetryLogListScrollToTopRequest = telemetryLogListScrollToTopRequest,
        otherListScrollToTopRequest = otherListScrollToTopRequest,
        telemetryLogContent = telemetryLogContent,
        otherContent = otherContent,
    )
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
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    hasAppUpdate: Boolean = false,
    accessLocalNetworkPermissionGranted: Boolean = true,
    keepScreenOn: Boolean = false,
    hapticFeedbackEnabled: Boolean = true,
    selectedSimulatorId: String = LMU_WINDOWS_SIMULATOR_ID,
    onSimulatorSelected: (String) -> Unit = {},
    onBannerTap: (() -> Unit)? = null,
    onFeedbackClick: ((Long) -> Unit)? = null,
    onReadoutTabReselected: () -> Unit = {},
    onLogTabReselected: () -> Unit = {},
    onOtherTabReselected: () -> Unit = {},
    readoutContent: @Composable (scrollToTopRequest: Int) -> Unit = {},
    readoutListScrollToTopRequest: Int = 0,
    telemetryLogContent: @Composable (scrollToTopRequest: Int, onFeedbackClick: (Long) -> Unit) -> Unit = { _, _ -> },
    telemetryLogListScrollToTopRequest: Int = 0,
    otherContent: @Composable (scrollToTopRequest: Int) -> Unit = {},
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
                snackbarHostState = snackbarHostState,
                navigationState = navigationState,
                onBannerTapWithTabSwitch = onBannerTapWithTabSwitch,
                onFeedbackClickWithTabSwitch = onFeedbackClickWithTabSwitch,
                onReadoutTabReselected = onReadoutTabReselected,
                onLogTabReselected = onLogTabReselected,
                onOtherTabReselected = onOtherTabReselected,
                readoutContent = readoutContent,
                readoutListScrollToTopRequest = readoutListScrollToTopRequest,
                telemetryLogContent = telemetryLogContent,
                telemetryLogListScrollToTopRequest = telemetryLogListScrollToTopRequest,
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
    snackbarHostState: SnackbarHostState,
    navigationState: AppNavigationState,
    onBannerTapWithTabSwitch: (() -> Unit)?,
    onFeedbackClickWithTabSwitch: ((Long) -> Unit)?,
    onReadoutTabReselected: () -> Unit,
    onLogTabReselected: () -> Unit,
    onOtherTabReselected: () -> Unit,
    readoutContent: @Composable (scrollToTopRequest: Int) -> Unit,
    readoutListScrollToTopRequest: Int,
    telemetryLogContent: @Composable (scrollToTopRequest: Int, onFeedbackClick: (Long) -> Unit) -> Unit,
    telemetryLogListScrollToTopRequest: Int,
    otherContent: @Composable (scrollToTopRequest: Int) -> Unit,
    otherListScrollToTopRequest: Int,
) {
    var simulatorMenuExpanded by remember { mutableStateOf(false) }
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding(),
    ) {
        NavigationSuiteScaffold(
            modifier = Modifier.padding(top = 4.dp),
            layoutType = resolvedLayoutType,
            navigationSuiteItems = {
                appScreenPrimarySimulatorNavItem(
                    resolvedLayoutType = resolvedLayoutType,
                    selectedSimulatorId = selectedSimulatorId,
                    expanded = simulatorMenuExpanded,
                    onExpandedChange = { simulatorMenuExpanded = it },
                    onSimulatorSelected = onSimulatorSelected,
                )
                AppDestination.entries.forEach { dest ->
                    val itemModifier =
                        if (resolvedLayoutType == NavigationSuiteType.NavigationDrawer) {
                            Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        } else {
                            Modifier
                        }
                    val showBadge =
                        dest == AppDestination.More &&
                            (hasAppUpdate || !accessLocalNetworkPermissionGranted)
                    item(
                        icon = {
                            if (resolvedLayoutType != NavigationSuiteType.NavigationDrawer) {
                                AppNavIcon(dest = dest, showBadge = showBadge)
                            }
                        },
                        label = {
                            if (resolvedLayoutType == NavigationSuiteType.NavigationDrawer) {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .offset(x = (-6).dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    AppNavIcon(
                                        dest = dest,
                                        showBadge = showBadge,
                                        modifier = Modifier.size(24.dp),
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(dest.label())
                                }
                            } else {
                                Text(dest.label())
                            }
                        },
                        selected = navigationState.current == dest,
                        onClick = {
                            navigationState.handleTabClick(dest) { reselected ->
                                when (reselected) {
                                    AppDestination.Readout -> onReadoutTabReselected()
                                    AppDestination.Log -> onLogTabReselected()
                                    AppDestination.More -> onOtherTabReselected()
                                }
                            }
                        },
                        modifier = itemModifier,
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
                AnimatedContent(
                    targetState = navigationState.current,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    modifier = Modifier.weight(1f),
                ) { destination ->
                    AppDestinationContent(
                        destination = destination,
                        readoutContent = { readoutContent(readoutListScrollToTopRequest) },
                        telemetryLogContent = {
                            telemetryLogContent(
                                telemetryLogListScrollToTopRequest,
                                onFeedbackClickWithTabSwitch ?: {},
                            )
                        },
                        otherContent = { otherContent(otherListScrollToTopRequest) },
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
                        start = 16.dp,
                        end = 16.dp,
                        bottom =
                            if (resolvedLayoutType == NavigationSuiteType.NavigationBar) {
                                96.dp
                            } else {
                                16.dp
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
internal fun ReadoutItemDetailContent(itemType: ReadoutListItemType) {
    when (itemType) {
        is ReadoutListItemType.LmuWindows -> LmuWindowsReadoutItemDetailContent(itemType)
        is ReadoutListItemType.Gt7Ps5 -> Gt7Ps5ReadoutItemDetailContent(itemType)
        is ReadoutListItemType.AceWindows -> AceWindowsReadoutItemDetailContent(itemType)
    }
}

@Composable
private fun LmuWindowsReadoutItemDetailContent(itemType: ReadoutListItemType.LmuWindows) {
    when (itemType) {
        ReadoutListItemType.LmuWindows.VehicleApproach -> LmuWindowsReadoutVehicleApproachDetailPane()
        ReadoutListItemType.LmuWindows.Flag -> LmuWindowsReadoutFlagDetailPane()
        ReadoutListItemType.LmuWindows.VehicleDamage -> LmuWindowsReadoutVehicleDamageDetailPane()
        ReadoutListItemType.LmuWindows.TyreTemperature -> LmuWindowsReadoutTyreTemperatureDetailPane()
        ReadoutListItemType.LmuWindows.PitTiming -> LmuWindowsReadoutPitTimingDetailPane()
        ReadoutListItemType.LmuWindows.RemainingVirtualEnergy -> LmuWindowsReadoutRemainingVirtualEnergyDetailPane()
        ReadoutListItemType.LmuWindows.TyreWear -> LmuWindowsReadoutTyreWearDetailPane()
        ReadoutListItemType.LmuWindows.MyBestLap -> LmuWindowsReadoutMyBestLapDetailPane()
    }
}

@Composable
private fun Gt7Ps5ReadoutItemDetailContent(itemType: ReadoutListItemType.Gt7Ps5) {
    when (itemType) {
        ReadoutListItemType.Gt7Ps5.MyBestLap -> {
            Gt7Ps5ReadoutMyBestLapDetailPane()
        }

        ReadoutListItemType.Gt7Ps5.RemainingFuelLaps -> {
            Gt7Ps5ReadoutRemainingFuelLapsDetailPane()
        }

        ReadoutListItemType.Gt7Ps5.RemainingFuel -> {
            Gt7Ps5ReadoutRemainingFuelDetailPane()
        }

        ReadoutListItemType.Gt7Ps5.TyreTemperature -> {
            Gt7Ps5ReadoutTyreTemperatureDetailPane()
        }
    }
}

@Composable
private fun AceWindowsReadoutItemDetailContent(itemType: ReadoutListItemType.AceWindows) {
    when (itemType) {
        ReadoutListItemType.AceWindows.Flag -> {
            AceWindowsReadoutFlagDetailPane()
        }

        ReadoutListItemType.AceWindows.TyreTemperature -> {
            AceWindowsReadoutTyreTemperatureDetailPane()
        }

        ReadoutListItemType.AceWindows.RemainingFuel -> {
            AceWindowsReadoutRemainingFuelDetailPane()
        }

        ReadoutListItemType.AceWindows.VehicleApproach -> {
            AceWindowsReadoutVehicleApproachDetailPane()
        }

        ReadoutListItemType.AceWindows.MyBestLap -> {
            AceWindowsReadoutMyBestLapDetailPane()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppScreenContentPreview() {
    AppScreenContent()
}
