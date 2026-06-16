package kurou.kodriver.presentation

import androidx.compose.animation.AnimatedContent
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
import kodriver.app.shared.generated.resources.lmu_connected
import kodriver.app.shared.generated.resources.lmu_disconnected
import kodriver.app.shared.generated.resources.nav_more
import kodriver.app.shared.generated.resources.nav_readout
import kurou.kodriver.feature.lmunarrator.LmuNarratorEffect
import kurou.kodriver.feature.lmureadout.flagdetail.LmuReadoutFlagDetailPane
import kurou.kodriver.feature.lmureadout.vehicleapproachdetail.LmuReadoutVehicleApproachDetailPane
import kurou.kodriver.feature.lmureadout.vehicledamagedetail.LmuReadoutVehicleDamageDetailPane
import kurou.kodriver.feature.otherlicensedetail.OtherLicenseDetailPane
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherserveripdetail.OtherServerIpDetailDialog
import kurou.kodriver.feature.othervolumedetail.OtherVolumeDetailPane
import kurou.kodriver.feature.readoutlist.ReadoutContent
import kurou.kodriver.feature.readoutlist.ReadoutListItemType
import org.jetbrains.compose.resources.stringResource

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
fun AppScreen(
    backHandler: @Composable (Boolean, () -> Unit) -> Unit = { _, _ -> },
    readoutContent: @Composable () -> Unit = {
        ReadoutContent(
            backHandler = backHandler,
            detailContent = { itemType ->
                when (itemType) {
                    ReadoutListItemType.VehicleApproach -> LmuReadoutVehicleApproachDetailPane()
                    ReadoutListItemType.Flag -> LmuReadoutFlagDetailPane()
                    ReadoutListItemType.VehicleDamage -> LmuReadoutVehicleDamageDetailPane()
                }
            },
        )
    },
    otherContent: @Composable () -> Unit = {
        var showServerIpDialog by rememberSaveable { mutableStateOf(false) }
        if (showServerIpDialog) {
            OtherServerIpDetailDialog(onDismiss = { showServerIpDialog = false })
        }
        OtherContent(
            backHandler = backHandler,
            onOpenServerIpDialog = { showServerIpDialog = true },
            detailContent = { itemType, canNavigateBack, onBack ->
                when (itemType) {
                    OtherListItemType.ServerIp -> {}
                    OtherListItemType.Volume -> OtherVolumeDetailPane(canNavigateBack, onBack)
                    OtherListItemType.GitHubRepository -> {}
                    OtherListItemType.ReleasePage -> {}
                    OtherListItemType.License -> OtherLicenseDetailPane(canNavigateBack, onBack)
                }
            },
        )
    },
) {
    val bannerUiState = rememberConnectionBannerUiState()
    val snackbarHostState = remember { SnackbarHostState() }
    val connectedMessage = stringResource(Res.string.lmu_connected)
    val disconnectedMessage = stringResource(Res.string.lmu_disconnected)

    ConnectionSnackbarEffect(
        isConnectionChecked = bannerUiState.isConnectionChecked,
        isConnected = bannerUiState.isConnected,
        snackbarHostState = snackbarHostState,
        connectedMessage = connectedMessage,
        disconnectedMessage = disconnectedMessage,
    )

    LmuNarratorEffect()
    AppScreenContent(
        bannerUiState = bannerUiState,
        snackbarHostState = snackbarHostState,
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
    readoutContent: @Composable () -> Unit = {},
    otherContent: @Composable () -> Unit = {},
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.Readout) }

    KoDriverTheme {
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val resolvedLayoutType = layoutType ?: when {
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
                NavigationSuiteType.NavigationDrawer
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
                NavigationSuiteType.NavigationRail
            else -> NavigationSuiteType.NavigationBar
        }
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
                        item(
                            icon = {
                                if (resolvedLayoutType != NavigationSuiteType.NavigationDrawer) {
                                    Icon(dest.icon, contentDescription = dest.label())
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
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = dest.label(),
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
                            onClick = { currentDestination = dest },
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
                    ConnectionBanner(uiState = bannerUiState)
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
