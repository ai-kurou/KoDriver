package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.lmu_connected
import kodriver.app.shared.generated.resources.lmu_disconnected
import kodriver.app.shared.generated.resources.nav_more
import kodriver.app.shared.generated.resources.nav_readout
import kurou.kodriver.feature.lmuconnection.LmuConnectionViewModel
import kurou.kodriver.feature.narrator.NarratorEffect
import kurou.kodriver.feature.other.OtherContent
import kurou.kodriver.feature.other.OtherItemType
import kurou.kodriver.feature.readout.ReadoutContent
import kurou.kodriver.feature.readout.ReadoutItemType
import kurou.kodriver.feature.readout.flag.FlagDetailPane
import kurou.kodriver.feature.readout.vehicleapproach.VehicleApproachDetailPane
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
                    ReadoutItemType.VehicleApproach -> VehicleApproachDetailPane()
                    ReadoutItemType.Flag -> FlagDetailPane()
                }
            },
        )
    },
    otherContent: @Composable () -> Unit = {
        OtherContent(
            backHandler = backHandler,
            detailContent = { itemType ->
                when (itemType) {
                    OtherItemType.GitHubRepository -> {}
                    OtherItemType.ReleasePage -> {}
                    OtherItemType.License -> LicenseDetailPane()
                }
            },
        )
    },
) {
    val connectionViewModel: LmuConnectionViewModel = koinViewModel()
    val connectionUiState by connectionViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val connectedMessage = stringResource(Res.string.lmu_connected)
    val disconnectedMessage = stringResource(Res.string.lmu_disconnected)

    ConnectionSnackbarEffect(
        isConnectionChecked = connectionUiState.isConnectionChecked,
        isConnected = connectionUiState.isConnected,
        snackbarHostState = snackbarHostState,
        connectedMessage = connectedMessage,
        disconnectedMessage = disconnectedMessage,
    )

    NarratorEffect()
    AppScreenContent(
        connectionStatus = if (connectionUiState.isConnected) {
            ConnectionStatus.Connected
        } else {
            ConnectionStatus.Waiting
        },
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
    LaunchedEffect(isConnectionChecked, isConnected) {
        if (isConnectionChecked) {
            snackbarHostState.showSnackbar(
                message = if (isConnected) connectedMessage else disconnectedMessage,
                duration = SnackbarDuration.Short,
            )
        }
    }
}

@Composable
internal fun AppScreenContent(
    layoutType: NavigationSuiteType? = null,
    connectionStatus: ConnectionStatus = ConnectionStatus.Waiting,
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
                layoutType = resolvedLayoutType,
                navigationSuiteItems = {
                    AppDestination.entries.forEach { dest ->
                        val itemModifier = if (resolvedLayoutType == NavigationSuiteType.NavigationDrawer) {
                            Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .padding(4.dp)
                                .widthIn(max = 240.dp)
                                .testTag("nav_${dest.name.lowercase()}")
                        } else {
                            Modifier.testTag("nav_${dest.name.lowercase()}")
                        }
                        item(
                            icon = { Icon(dest.icon, contentDescription = dest.label()) },
                            label = { Text(dest.label()) },
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
                Box(modifier = contentModifier) {
                    when (currentDestination) {
                        AppDestination.Readout -> readoutContent()
                        AppDestination.More -> otherContent()
                    }
                }
            }
            ConnectionStatusPlacement(
                layoutType = resolvedLayoutType,
                status = connectionStatus,
                modifier = Modifier.align(Alignment.BottomStart),
            )
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

@Composable
private fun ConnectionStatusPlacement(
    layoutType: NavigationSuiteType,
    status: ConnectionStatus,
    modifier: Modifier = Modifier,
) {
    val navigationWidth = when (layoutType) {
        NavigationSuiteType.NavigationRail -> 80.dp
        NavigationSuiteType.NavigationDrawer -> 360.dp
        else -> null
    }
    Box(
        modifier = modifier
            .then(navigationWidth?.let { Modifier.width(it) } ?: Modifier)
            .padding(
                start = if (layoutType == NavigationSuiteType.NavigationBar) 16.dp else 0.dp,
                bottom = if (layoutType == NavigationSuiteType.NavigationBar) 96.dp else 24.dp,
            ),
        contentAlignment = if (layoutType == NavigationSuiteType.NavigationBar) {
            Alignment.BottomStart
        } else {
            Alignment.BottomCenter
        },
    ) {
        ConnectionStatusIndicator(status = status)
    }
}

@Preview(showBackground = true)
@Composable
private fun AppScreenContentPreview() {
    AppScreenContent(connectionStatus = ConnectionStatus.Connected)
}
