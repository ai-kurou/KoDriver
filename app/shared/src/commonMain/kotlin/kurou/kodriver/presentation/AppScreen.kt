package kurou.kodriver.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.nav_more
import kodriver.app.shared.generated.resources.nav_readout
import kurou.kodriver.feature.gt7ps5narrator.Gt7Ps5NarratorEffect
import kurou.kodriver.feature.gt7ps5readout.mybestlapdetail.Gt7Ps5ReadoutMyBestLapDetailPane
import kurou.kodriver.feature.lmuwindowsnarrator.LmuWindowsNarratorEffect
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.LmuWindowsReadoutFlagDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.LmuWindowsReadoutVehicleApproachDetailPane
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.LmuWindowsReadoutVehicleDamageDetailPane
import kurou.kodriver.feature.main.AppScreenViewModel
import kurou.kodriver.feature.otherconsoleipdetail.OtherConsoleIpDetailPane
import kurou.kodriver.feature.otherkeepscreenondetail.OtherKeepScreenOnDetailDialog
import kurou.kodriver.feature.otherlicensedetail.OtherLicenseDetailPane
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListViewModel
import kurou.kodriver.feature.otherreadoutstartsounddetail.OtherReadoutStartSoundDetailDialog
import kurou.kodriver.feature.otherserveripdetail.OtherServerIpDetailPane
import kurou.kodriver.feature.othervolumedetail.OtherVolumeDetailPane
import kurou.kodriver.feature.readoutlist.ReadoutContent
import kurou.kodriver.feature.readoutlist.ReadoutListItemType
import kurou.kodriver.feature.readoutlist.ReadoutListViewModel
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

private fun handleTabClick(
    dest: AppDestination,
    currentDestination: AppDestination,
    onReadoutTabReselected: () -> Unit,
    onOtherTabReselected: () -> Unit,
    setCurrentDestination: (AppDestination) -> Unit,
) {
    if (currentDestination == dest) {
        when (dest) {
            AppDestination.Readout -> onReadoutTabReselected()
            AppDestination.More -> onOtherTabReselected()
        }
    }
    setCurrentDestination(dest)
}

private enum class AppDestination(
    val icon: ImageVector,
) {
    Readout(Icons.Default.HeadsetMic),
    More(Icons.Default.MoreHoriz),
}

@Composable
private fun AppDestination.label(): String = when (this) {
    AppDestination.Readout -> stringResource(Res.string.nav_readout)
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
    backHandler: @Composable (Boolean, () -> Unit) -> Unit,
) {
    var showReadoutStartSoundDialog by rememberSaveable { mutableStateOf(false) }
    var showKeepScreenOnDialog by rememberSaveable { mutableStateOf(false) }
    if (showReadoutStartSoundDialog) {
        OtherReadoutStartSoundDetailDialog(onDismiss = { showReadoutStartSoundDialog = false })
    }
    if (showKeepScreenOnDialog) {
        OtherKeepScreenOnDetailDialog(onDismiss = { showKeepScreenOnDialog = false })
    }
    OtherContent(
        backHandler = backHandler,
        onOpenReadoutStartSoundDialog = { showReadoutStartSoundDialog = true },
        onOpenKeepScreenOnDialog = { showKeepScreenOnDialog = true },
        detailContent = { itemType, canNavigateBack, onBack ->
            when (itemType) {
                OtherListItemType.ServerIp -> OtherServerIpDetailPane(canNavigateBack, onBack)
                OtherListItemType.ConsoleIp -> OtherConsoleIpDetailPane(canNavigateBack, onBack)
                OtherListItemType.Volume -> OtherVolumeDetailPane(canNavigateBack, onBack)
                OtherListItemType.License -> OtherLicenseDetailPane(canNavigateBack, onBack)
                else -> {}
            }
        },
    )
}

@Composable
fun AppScreen(
    viewModel: AppScreenViewModel = koinViewModel(),
    readoutListViewModel: ReadoutListViewModel = koinViewModel(),
    otherListViewModel: OtherListViewModel = koinViewModel(),
    backHandler: @Composable (Boolean, () -> Unit) -> Unit = { _, _ -> },
    readoutContent: @Composable () -> Unit = {
        ReadoutContent(
            backHandler = backHandler,
            detailContent = { itemType ->
                when (itemType) {
                    ReadoutListItemType.LmuWindows.VehicleApproach -> LmuWindowsReadoutVehicleApproachDetailPane()
                    ReadoutListItemType.LmuWindows.Flag -> LmuWindowsReadoutFlagDetailPane()
                    ReadoutListItemType.LmuWindows.VehicleDamage -> LmuWindowsReadoutVehicleDamageDetailPane()
                    ReadoutListItemType.Gt7Ps5.MyBestLap -> Gt7Ps5ReadoutMyBestLapDetailPane()
                    ReadoutListItemType.Gt7Ps5.RemainingFuelLaps -> {}
                }
            },
        )
    },
    otherContent: @Composable () -> Unit = {
        DefaultOtherContent(backHandler = backHandler)
    },
) {
    val bannerUiState = rememberConnectionBannerUiState()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()

    val onBannerTap = if (bannerUiState.isTappable && bannerUiState.tapNavigationItemId != null) {
        {
            OtherListItemType.fromId(bannerUiState.tapNavigationItemId)
                ?.let { otherListViewModel.selectItem(it) }
            Unit
        }
    } else {
        null
    }

    LaunchedEffect(Unit) {
        viewModel.checkUpdate()
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
        bannerUiState = bannerUiState,
        snackbarHostState = snackbarHostState,
        hasAppUpdate = uiState.hasAppUpdate,
        keepScreenOn = uiState.keepScreenOn,
        onBannerTap = onBannerTap,
        onReadoutTabReselected = readoutListViewModel::clearSelectedItem,
        onOtherTabReselected = otherListViewModel::clearSelectedItem,
        readoutContent = readoutContent,
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
    layoutType: NavigationSuiteType? = null,
    bannerUiState: ConnectionBannerUiState = ConnectionBannerUiState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    hasAppUpdate: Boolean = false,
    keepScreenOn: Boolean = false,
    onBannerTap: (() -> Unit)? = null,
    onReadoutTabReselected: () -> Unit = {},
    onOtherTabReselected: () -> Unit = {},
    readoutContent: @Composable () -> Unit = {},
    otherContent: @Composable () -> Unit = {},
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.Readout) }
    val onBannerTapWithTabSwitch = bannerTapWithTabSwitch(onBannerTap) {
        currentDestination = AppDestination.More
    }

    KoDriverTheme {
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val resolvedLayoutType = layoutType ?: when {
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
                NavigationSuiteType.NavigationDrawer
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
                NavigationSuiteType.NavigationRail
            else -> NavigationSuiteType.NavigationBar
        }
        KeepScreenOnEffect(keepScreenOn)
        Box(modifier = Modifier.navigationBarsPadding()) {
            NavigationSuiteScaffold(
                modifier = Modifier.padding(top = 4.dp),
                layoutType = resolvedLayoutType,
                navigationSuiteItems = {
                    AppDestination.entries.forEach { dest ->
                        val itemModifier = if (resolvedLayoutType == NavigationSuiteType.NavigationDrawer) {
                            Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .testTag("nav_${dest.name.lowercase()}")
                        } else {
                            Modifier.testTag("nav_${dest.name.lowercase()}")
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
                    .statusBarsPadding()
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
                        when (destination) {
                            AppDestination.Readout -> readoutContent()
                            AppDestination.More -> otherContent()
                        }
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

@Preview(showBackground = true)
@Composable
private fun AppScreenContentPreview() {
    AppScreenContent()
}
