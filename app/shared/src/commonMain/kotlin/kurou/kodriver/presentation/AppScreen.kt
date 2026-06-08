package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.nav_more
import kodriver.app.shared.generated.resources.nav_readout
import kurou.kodriver.feature.narrator.NarratorEffect
import kurou.kodriver.feature.other.OtherContent
import kurou.kodriver.feature.readout.ReadoutContent
import kurou.kodriver.feature.readout.ReadoutItemType
import kurou.kodriver.feature.readout.vehicleapproach.VehicleApproachDetailPane
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
                    ReadoutItemType.VehicleApproach -> VehicleApproachDetailPane()
                    ReadoutItemType.LapsRemaining -> {}
                }
            },
        )
    },
) {
    NarratorEffect()
    AppScreenContent(readoutContent = readoutContent)
}

@Composable
private fun AppScreenContent(
    readoutContent: @Composable () -> Unit = {},
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.Readout) }

    KoDriverTheme {
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val layoutType = when {
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
                NavigationSuiteType.NavigationDrawer
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
                NavigationSuiteType.NavigationRail
            else -> NavigationSuiteType.NavigationBar
        }
        Box(modifier = Modifier.navigationBarsPadding()) {
            NavigationSuiteScaffold(
                layoutType = layoutType,
                navigationSuiteItems = {
                    AppDestination.entries.forEach { dest ->
                        val itemModifier = if (layoutType == NavigationSuiteType.NavigationDrawer) {
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
                Box(modifier = Modifier.statusBarsPadding()) {
                    when (currentDestination) {
                        AppDestination.Readout -> readoutContent()
                        AppDestination.More -> OtherContent()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppScreenContentPreview() {
    AppScreenContent()
}
