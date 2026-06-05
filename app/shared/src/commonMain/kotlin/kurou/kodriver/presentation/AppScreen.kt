package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.nav_more
import kodriver.app.shared.generated.resources.nav_readout
import kurou.kodriver.domain.model.ReadoutItemType
import kurou.kodriver.feature.other.OtherContent
import kurou.kodriver.feature.readout.ReadoutContent
import kurou.kodriver.feature.readout.vehicleapproach.VehicleApproachDetailPane
import org.jetbrains.compose.resources.stringResource

internal enum class AppDestination(
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
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.Readout) }

    KoDriverTheme {
        Box(modifier = Modifier.navigationBarsPadding()) {
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    AppDestination.entries.forEach { dest ->
                        item(
                            icon = { Icon(dest.icon, contentDescription = dest.label()) },
                            label = { Text(dest.label()) },
                            selected = currentDestination == dest,
                            onClick = { currentDestination = dest },
                            modifier = Modifier.testTag("nav_${dest.name.lowercase()}"),
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

@Preview
@Composable
private fun AppScreenPreview() {
    AppScreen()
}
