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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.MoreHoriz
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
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.nav_log
import kodriver.app.shared.generated.resources.nav_more
import kodriver.app.shared.generated.resources.nav_readout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kurou.kodriver.feature.gt7ps5narrator.Gt7Ps5NarratorEffect
import kurou.kodriver.feature.gt7ps5readout.mybestlapdetail.Gt7Ps5ReadoutMyBestLapDetailPane
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.Gt7Ps5ReadoutRemainingFuelLapsDetailPane
import kurou.kodriver.feature.lmuwindowsnarrator.LmuWindowsNarratorEffect
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.LmuWindowsReadoutFlagDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail.LmuWindowsReadoutMyBestLapDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.LmuWindowsReadoutTyreTemperatureDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.LmuWindowsReadoutVehicleApproachDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.LmuWindowsReadoutVehicleDamageDetailPane
import kurou.kodriver.feature.main.AppScreenViewModel
import kurou.kodriver.feature.otherconsoleipdetail.OtherConsoleIpDetailPane
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

private fun bannerTapWithTabSwitch(
    onBannerTap: (() -> Unit)?,
    switchToMore: () -> Unit,
): (() -> Unit)? = if (onBannerTap != null) {
    {
        switchToMore()
        onBannerTap()
    }
} else {
    null
}

private fun ConnectionBannerNavigationTarget.toOtherListItemType(): OtherListItemType = when (this) {
    ConnectionBannerNavigationTarget.ConsoleIp -> OtherListItemType.ConsoleIp
    ConnectionBannerNavigationTarget.ServerIp -> OtherListItemType.ServerIp
}

private fun handleTabClick(
    dest: AppDestination,
    currentDestination: AppDestination,
    onReadoutTabReselected: () -> Unit,
    onLogTabReselected: () -> Unit,
    onOtherTabReselected: () -> Unit,
    setCurrentDestination: (AppDestination) -> Unit,
) {
    if (currentDestination == dest) {
        when (dest) {
            AppDestination.Readout -> onReadoutTabReselected()
            AppDestination.Log -> onLogTabReselected()
            AppDestination.More -> onOtherTabReselected()
        }
    }
    setCurrentDestination(dest)
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

private enum class AppDestination(
    val icon: ImageVector,
) {
    Readout(Icons.Default.HeadsetMic),
    Log(Icons.Default.Description),
    More(Icons.Default.MoreHoriz),
}

@Composable
private fun AppDestination.label(): String = when (this) {
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

@Composable
private fun DefaultOtherContent(
    backHandler: AppBackHandler,
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
        onOpenReadoutStartSoundDialog = { showReadoutStartSoundDialog = true },
        onOpenThemeDialog = { showThemeDialog = true },
        detailContent = { itemType, canNavigateBack, onBack ->
            when (itemType) {
                OtherListItemType.ServerIp -> OtherServerIpDetailPane(canNavigateBack, onBack)
                OtherListItemType.ConsoleIp -> OtherConsoleIpDetailPane(canNavigateBack, onBack)
                OtherListItemType.Volume -> OtherVolumeDetailPane(canNavigateBack, onBack)
                OtherListItemType.License -> OtherLicenseDetailPane(canNavigateBack, onBack)
                OtherListItemType.KeepScreenOn,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.ExitConfirmation,
                OtherListItemType.Theme,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                -> {}
            }
        },
    )
}

@Composable
fun AppScreen(
    viewModel: AppScreenViewModel = koinViewModel(),
    readoutListViewModel: ReadoutListViewModel = koinViewModel(),
    telemetryLogListViewModel: TelemetryLogListViewModel = koinViewModel(),
    otherListViewModel: OtherListViewModel = koinViewModel(),
    backHandler: AppBackHandler = { _, _, _ -> },
    onExit: () -> Unit = {},
    exitRequested: Boolean = false,
    onExitRequestConsumed: () -> Unit = {},
    onDarkThemeChanged: (Boolean) -> Unit = {},
    readoutContent: @Composable () -> Unit = {
        ReadoutContent(
            backHandler = backHandler,
            detailContent = { itemType -> ReadoutItemDetailContent(itemType) },
        )
    },
    telemetryLogContent: @Composable () -> Unit = {
        TelemetryLogContent(
            backHandler = backHandler,
            detailContent = { id ->
                TelemetryLogDetailContent(id = id)
            },
        )
    },
    otherContent: @Composable () -> Unit = {
        DefaultOtherContent(backHandler = backHandler)
    },
) {
    val darkTheme = rememberAppDarkTheme()
    val bannerUiState = rememberConnectionBannerUiState()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showExitConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    val onBannerTap = if (bannerUiState.isTappable && bannerUiState.tapNavigationTarget != null) {
        {
            otherListViewModel.selectItem(bannerUiState.tapNavigationTarget.toOtherListItemType())
            Unit
        }
    } else {
        null
    }

    LaunchedEffect(Unit) {
        viewModel.checkUpdate()
    }

    LaunchedEffect(darkTheme) {
        onDarkThemeChanged(darkTheme)
    }

    LaunchedEffect(exitRequested) {
        if (exitRequested) {
            onExitRequestConsumed()
            if (uiState.exitConfirmationEnabled) {
                showExitConfirmationDialog = true
            } else {
                onExit()
            }
        }
    }

    backHandler(uiState.exitConfirmationEnabled, {}) {
        showExitConfirmationDialog = true
    }

    if (showExitConfirmationDialog) {
        AppTheme(darkTheme = darkTheme) {
            ExitConfirmationDialog(
                onDismiss = { showExitConfirmationDialog = false },
                onConfirm = { doNotShowAgain ->
                    coroutineScope.launch {
                        saveExitConfirmationPreferenceForExit(
                            doNotShowAgain = doNotShowAgain,
                            saveExitConfirmationEnabled = viewModel::saveExitConfirmationEnabled,
                        )
                        showExitConfirmationDialog = false
                        onExit()
                    }
                },
            )
        }
    }

    ConnectionSnackbarEffect(
        isConnectionChecked = bannerUiState.isConnectionChecked,
        isConnected = bannerUiState.isConnected,
        snackbarHostState = snackbarHostState,
        connectedMessage = bannerUiState.snackbarConnectedMessage,
        disconnectedMessage = bannerUiState.snackbarDisconnectedMessage,
    )

    LmuWindowsNarratorEffect()
    Gt7Ps5NarratorEffect()
    VersionMismatchBottomSheetEffect()
    AppScreenContent(
        darkTheme = darkTheme,
        bannerUiState = bannerUiState,
        snackbarHostState = snackbarHostState,
        hasAppUpdate = uiState.hasAppUpdate,
        keepScreenOn = uiState.keepScreenOn,
        onBannerTap = onBannerTap,
        onReadoutTabReselected = readoutListViewModel::clearSelectedItem,
        onLogTabReselected = telemetryLogListViewModel::clearSelectedLog,
        onOtherTabReselected = otherListViewModel::clearSelectedItem,
        readoutContent = readoutContent,
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

internal suspend fun saveExitConfirmationPreferenceForExit(
    doNotShowAgain: Boolean,
    saveExitConfirmationEnabled: suspend (Boolean) -> Unit,
) {
    if (!doNotShowAgain) return
    try {
        saveExitConfirmationEnabled(false)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Throwable) {
    }
}

@Composable
internal fun AppScreenContent(
    darkTheme: Boolean = false,
    layoutType: NavigationSuiteType? = null,
    bannerUiState: ConnectionBannerUiState = ConnectionBannerUiState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    hasAppUpdate: Boolean = false,
    keepScreenOn: Boolean = false,
    onBannerTap: (() -> Unit)? = null,
    onReadoutTabReselected: () -> Unit = {},
    onLogTabReselected: () -> Unit = {},
    onOtherTabReselected: () -> Unit = {},
    readoutContent: @Composable () -> Unit = {},
    telemetryLogContent: @Composable () -> Unit = {},
    otherContent: @Composable () -> Unit = {},
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.Readout) }
    val onBannerTapWithTabSwitch = bannerTapWithTabSwitch(onBannerTap) {
        currentDestination = AppDestination.More
    }

    AppTheme(darkTheme = darkTheme) {
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val resolvedLayoutType = layoutType ?: windowSizeClass.resolveNavigationSuiteType()
        KeepScreenOnEffect(keepScreenOn)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding(),
        ) {
            NavigationSuiteScaffold(
                modifier = Modifier.padding(top = 4.dp),
                layoutType = resolvedLayoutType,
                navigationSuiteItems = {
                    AppDestination.entries.forEach { dest ->
                        val itemModifier = if (resolvedLayoutType == NavigationSuiteType.NavigationDrawer) {
                            Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        } else {
                            Modifier
                        }
                        val showBadge = dest == AppDestination.More && hasAppUpdate
                        item(
                            icon = {
                                if (resolvedLayoutType != NavigationSuiteType.NavigationDrawer) {
                                    AppNavIcon(dest = dest, showBadge = showBadge)
                                }
                            },
                            label = {
                                if (resolvedLayoutType == NavigationSuiteType.NavigationDrawer) {
                                    Row(
                                        modifier = Modifier
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
                            selected = currentDestination == dest,
                            onClick = {
                                handleTabClick(
                                    dest = dest,
                                    currentDestination = currentDestination,
                                    onReadoutTabReselected = onReadoutTabReselected,
                                    onLogTabReselected = onLogTabReselected,
                                    onOtherTabReselected = onOtherTabReselected,
                                    setCurrentDestination = { currentDestination = it },
                                )
                            },
                            modifier = itemModifier,
                        )
                    }
                },
            ) {
                val dividerColor = DividerDefaults.color
                val dividerThickness = DividerDefaults.Thickness
                val contentModifier = Modifier
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
                    AnimatedVisibility(visible = bannerUiState.isVisible) {
                        ConnectionBannerContent(
                            uiState = bannerUiState,
                            onClick = onBannerTapWithTabSwitch,
                        )
                    }
                    AnimatedContent(
                        targetState = currentDestination,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        modifier = Modifier.weight(1f),
                    ) { destination ->
                        AppDestinationContent(
                            destination = destination,
                            readoutContent = readoutContent,
                            telemetryLogContent = telemetryLogContent,
                            otherContent = otherContent,
                        )
                    }
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (resolvedLayoutType == NavigationSuiteType.NavigationBar) {
                            96.dp
                        } else {
                            16.dp
                        },
                    ),
            )
        }
    }
}

internal fun WindowSizeClass.resolveNavigationSuiteType(): NavigationSuiteType = when {
    isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
        NavigationSuiteType.NavigationRail
    else -> NavigationSuiteType.NavigationBar
}

@Composable
private fun ReadoutItemDetailContent(itemType: ReadoutListItemType) {
    when (itemType) {
        ReadoutListItemType.LmuWindows.VehicleApproach -> LmuWindowsReadoutVehicleApproachDetailPane()
        ReadoutListItemType.LmuWindows.Flag -> LmuWindowsReadoutFlagDetailPane()
        ReadoutListItemType.LmuWindows.VehicleDamage -> LmuWindowsReadoutVehicleDamageDetailPane()
        ReadoutListItemType.LmuWindows.TyreTemperature -> LmuWindowsReadoutTyreTemperatureDetailPane()
        ReadoutListItemType.LmuWindows.MyBestLap -> LmuWindowsReadoutMyBestLapDetailPane()
        ReadoutListItemType.Gt7Ps5.MyBestLap -> Gt7Ps5ReadoutMyBestLapDetailPane()
        ReadoutListItemType.Gt7Ps5.RemainingFuelLaps -> Gt7Ps5ReadoutRemainingFuelLapsDetailPane()
    }
}

@Preview(showBackground = true)
@Composable
private fun AppScreenContentPreview() {
    AppScreenContent()
}
