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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.domain.model.TelemetryLog
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TelemetryLogContent(
    modifier: Modifier = Modifier,
    scaffoldDirective: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
) {
    val viewModel = koinViewModel<TelemetryLogListViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TelemetryLogContentScaffold(
        uiState = uiState,
        modifier = modifier,
        scaffoldDirective = scaffoldDirective,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun TelemetryLogContentScaffold(
    uiState: TelemetryLogListUiState = TelemetryLogListUiState(),
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
            TelemetryLogListPane(uiState = uiState)
        },
        detailPane = {
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(showBackground = true)
@Composable
private fun TelemetryLogContentPreview() {
    TelemetryLogContentScaffold(
        uiState = previewTelemetryLogListUiState,
    )
}

internal val previewTelemetryLogListUiState = TelemetryLogListUiState(
    logs = listOf(
        TelemetryLog(
            id = 3,
            createdAt = 1_820_000,
            simulatorId = "lmu_windows",
            readoutItemKey = "flag",
            telemetryJson = """{"flag":"green","sector1":"clear","sector2":"clear","sector3":"clear"}""",
        ),
        TelemetryLog(
            id = 2,
            createdAt = 1_810_000,
            simulatorId = "lmu_windows",
            readoutItemKey = "vehicle_approach",
            telemetryJson = """{"left":false,"right":true,"distanceMeters":12.4}""",
        ),
        TelemetryLog(
            id = 1,
            createdAt = 1_800_000,
            simulatorId = "gt7_ps5",
            readoutItemKey = "remaining_fuel_laps",
            telemetryJson = """{"remainingFuelLaps":3.6,"fuelPercent":18.2}""",
        ),
    ),
)
