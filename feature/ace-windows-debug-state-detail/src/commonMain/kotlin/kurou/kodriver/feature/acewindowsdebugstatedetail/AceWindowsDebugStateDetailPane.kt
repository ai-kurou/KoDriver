package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DebugReadoutRequestCard
import kurou.kodriver.core.designsystem.DebugStateCard
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_best_lap_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_flag_info_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_fuel_consumption_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_side_by_side_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_simulator_info_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_tyre_carcass_temperature_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_vehicle_location_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.navigate_back
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

private val NARROW_WIDTH_UPPER_BOUND = 400.dp
private val MEDIUM_WIDTH_UPPER_BOUND = 700.dp

internal const val DEBUG_STATE_GRID_TEST_TAG = "debug_state_grid"

internal fun calculateDebugStateColumns(maxWidth: Dp): Int =
    when {
        maxWidth < NARROW_WIDTH_UPPER_BOUND -> 1
        maxWidth < MEDIUM_WIDTH_UPPER_BOUND -> 2
        else -> 3
    }

/**
 * ACE Windows 専用のデバッグ状態画面を表示する Composable。
 */
@Composable
fun AceWindowsDebugStateDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AceWindowsDebugStateDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AceWindowsDebugStateDetailPaneContent(
        uiState = uiState,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        onMoveCard = viewModel::moveCard,
        onReadoutItemSelected = viewModel::selectReadoutItem,
        onDebugReadoutTextChange = viewModel::updateDebugReadoutText,
        onRequestReadout = viewModel::requestReadout,
        modifier = modifier,
    )
}

@Composable
internal fun AceWindowsDebugStateDetailPaneContent(
    uiState: AceWindowsDebugStateDetailUiState,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    onMoveCard: (Int, Int) -> Unit = { _, _ -> },
    onReadoutItemSelected: (String) -> Unit = {},
    onDebugReadoutTextChange: (String) -> Unit = {},
    onRequestReadout: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    DetailPaneScaffold(
        title = stringResource(Res.string.debug_state_title),
        canNavigateBack = canNavigateBack,
        navigateBackContentDescription = stringResource(Res.string.navigate_back),
        onBack = onBack,
        modifier = modifier,
    ) {
        BoxWithConstraints {
            val columns = calculateDebugStateColumns(maxWidth)
            val gridState = rememberLazyGridState()
            val reorderableState =
                rememberReorderableLazyGridState(gridState) { from, to ->
                    onMoveCard(from.index, to.index)
                }
            Column {
                DebugReadoutRequestCard(
                    readoutItemKeyValues = ReadoutItemKey.topLevelEntriesOf(Simulator.AceWindows).map { it.value },
                    selectedReadoutItemKeyValue = uiState.selectedReadoutItemKey.value,
                    text = uiState.debugReadoutText,
                    onReadoutItemSelected = onReadoutItemSelected,
                    onTextChange = onDebugReadoutTextChange,
                    onReadout = onRequestReadout,
                    modifier = Modifier.padding(KoDriverSpacing.small),
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    state = gridState,
                    modifier = Modifier.testTag(DEBUG_STATE_GRID_TEST_TAG),
                ) {
                    items(uiState.cardOrder, key = { it.name }) { cardKey ->
                        ReorderableItem(reorderableState, key = cardKey.name) {
                            DebugStateCard(
                                cardKey = cardKey,
                                uiState = uiState,
                                enabled = cardKey in uiState.enabledCardKeys,
                                titles = debugStateCardTitles,
                                contents = debugStateCardContents,
                                frame = { title, cardModifier, content ->
                                    DebugStateCardFrame(
                                        cardKey = cardKey,
                                        title = title,
                                        modifier = cardModifier,
                                        content = content,
                                    )
                                },
                                modifier = Modifier.padding(KoDriverSpacing.small).longPressDraggableHandle(),
                            )
                        }
                    }
                }
            }
        }
    }
}

private val debugStateCardTitles: Map<AceWindowsDebugStateCardKey, StringResource> =
    mapOf(
        AceWindowsDebugStateCardKey.SIMULATOR to Res.string.debug_state_simulator_info_title,
        AceWindowsDebugStateCardKey.VEHICLE_LOCATION to Res.string.debug_state_vehicle_location_title,
        AceWindowsDebugStateCardKey.FLAG_INFO to Res.string.debug_state_flag_info_title,
        AceWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES to Res.string.debug_state_side_by_side_title,
        AceWindowsDebugStateCardKey.BEST_LAP to Res.string.debug_state_best_lap_title,
        AceWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE to Res.string.debug_state_tyre_carcass_temperature_title,
        AceWindowsDebugStateCardKey.FUEL_CONSUMPTION to Res.string.debug_state_fuel_consumption_title,
    )

private val debugStateCardContents:
    Map<AceWindowsDebugStateCardKey, @Composable (AceWindowsDebugStateDetailUiState) -> Unit> =
    mapOf(
        AceWindowsDebugStateCardKey.SIMULATOR to { SimulatorInfoContent() },
        AceWindowsDebugStateCardKey.VEHICLE_LOCATION to { uiState ->
            VehicleLocationContent(uiState.aceWindows.status)
        },
        AceWindowsDebugStateCardKey.FLAG_INFO to { uiState -> FlagInfoContent(uiState.aceWindows.flag) },
        AceWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES to
            { uiState ->
                SideBySideVehiclesContent(uiState.aceWindows.vehicleApproach)
            },
        AceWindowsDebugStateCardKey.BEST_LAP to { uiState -> BestLapContent(uiState.aceWindows.bestLapTime) },
        AceWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE to
            { uiState ->
                TyreCarcassTemperatureContent(uiState.aceWindows.tyreCarcassTemperature)
            },
        AceWindowsDebugStateCardKey.FUEL_CONSUMPTION to { uiState ->
            FuelConsumptionContent(uiState.aceWindows.fuel, uiState.aceWindows.remainingFuelLaps)
        },
    )

@Preview(showBackground = true)
@Composable
private fun AceWindowsDebugStateDetailPanePreview() {
    KoDriverTheme {
        AceWindowsDebugStateDetailPaneContent(
            uiState = AceWindowsDebugStateDetailUiState(),
            canNavigateBack = true,
            onBack = {},
        )
    }
}
