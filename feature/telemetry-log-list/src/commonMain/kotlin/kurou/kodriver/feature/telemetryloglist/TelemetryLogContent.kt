package kurou.kodriver.feature.telemetryloglist

import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TelemetryLogContent(
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
) {
    koinViewModel<TelemetryLogListViewModel>()
    TelemetryLogContentScaffold(
        modifier = modifier,
        scaffoldDirective = scaffoldDirective,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun TelemetryLogContentScaffold(
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>(
        scaffoldDirective = scaffoldDirective.copy(maxHorizontalPartitions = 1),
        initialDestinationHistory = listOf(ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List)),
    )
    val paneExpansionState = rememberPaneExpansionState(
        anchors = listOf(PaneExpansionAnchor.Offset.fromStart(350.dp)),
        initialAnchoredIndex = 0,
    )

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        scaffoldState = navigator.scaffoldState,
        paneExpansionState = paneExpansionState,
        paneExpansionDragHandle = { VerticalDivider() },
        modifier = modifier,
        listPane = {
            TelemetryLogListPane()
        },
        detailPane = {
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(showBackground = true)
@Composable
private fun TelemetryLogContentPreview() {
    TelemetryLogContentScaffold()
}
