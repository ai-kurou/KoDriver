package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_best_lap_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_current_lap_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_pit_timing_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_session_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_side_by_side_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_simulator_info_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_carcass_temperature_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_temperature_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_wear_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_class_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_title
import kurou.kodriver.feature.debugstatedetail.generated.resources.navigate_back
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

private val NARROW_WIDTH_UPPER_BOUND = 400.dp
private val MEDIUM_WIDTH_UPPER_BOUND = 700.dp
private const val DISABLED_CARD_ALPHA = 0.38f

internal const val DEBUG_STATE_GRID_TEST_TAG = "debug_state_grid"

internal fun calculateDebugStateColumns(maxWidth: Dp): Int =
    when {
        maxWidth < NARROW_WIDTH_UPPER_BOUND -> 1
        maxWidth < MEDIUM_WIDTH_UPPER_BOUND -> 2
        else -> 3
    }

/**
 * DebugStateDetail の画面を表示する Composable。
 */
@Composable
fun DebugStateDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DebugStateDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DebugStateDetailPaneContent(
        uiState = uiState,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        onMoveCard = viewModel::moveCard,
        modifier = modifier,
    )
}

@Composable
fun DebugStateDetailPaneContent(
    uiState: DebugStateDetailUiState,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    onMoveCard: (Int, Int) -> Unit = { _, _ -> },
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
                            modifier = Modifier.padding(8.dp).longPressDraggableHandle(),
                        )
                    }
                }
            }
        }
    }
}

private val debugStateCardTitles: Map<DebugStateCardKey, StringResource> =
    mapOf(
        DebugStateCardKey.SIMULATOR to Res.string.debug_state_simulator_info_title,
        DebugStateCardKey.VEHICLE_CLASS to Res.string.debug_state_vehicle_class_title,
        DebugStateCardKey.VEHICLE_LOCATION to Res.string.debug_state_vehicle_location_title,
        DebugStateCardKey.FLAG_INFO to Res.string.debug_state_flag_info_title,
        DebugStateCardKey.GAME_PHASE to Res.string.debug_state_game_phase_title,
        DebugStateCardKey.SESSION to Res.string.debug_state_session_title,
        DebugStateCardKey.YELLOW_FLAG_STATE to Res.string.debug_state_yellow_flag_state_title,
        DebugStateCardKey.CURRENT_LAP to Res.string.debug_state_current_lap_title,
        DebugStateCardKey.SIDE_BY_SIDE_VEHICLES to Res.string.debug_state_side_by_side_title,
        DebugStateCardKey.BEST_LAP to Res.string.debug_state_best_lap_title,
        DebugStateCardKey.TYRE_TEMPERATURE to Res.string.debug_state_tyre_temperature_title,
        DebugStateCardKey.TYRE_CARCASS_TEMPERATURE to Res.string.debug_state_tyre_carcass_temperature_title,
        DebugStateCardKey.TYRE_WEAR to Res.string.debug_state_tyre_wear_title,
        DebugStateCardKey.FUEL_CONSUMPTION to Res.string.debug_state_fuel_consumption_title,
        DebugStateCardKey.PIT_TIMING_REMAINING_LAPS to Res.string.debug_state_pit_timing_title,
    )

private val debugStateCardContents: Map<DebugStateCardKey, @Composable (DebugStateDetailUiState) -> Unit> =
    mapOf(
        DebugStateCardKey.SIMULATOR to { uiState -> SimulatorInfoContent(uiState.selectedSimulator) },
        DebugStateCardKey.VEHICLE_CLASS to
            { uiState ->
                VehicleClassContent(
                    uiState.selectedSimulator,
                    uiState.lmuWindowsVehicleClass,
                    uiState.gt7Ps5VehicleClass,
                )
            },
        DebugStateCardKey.VEHICLE_LOCATION to
            { uiState ->
                VehicleLocationContent(
                    uiState.selectedSimulator,
                    uiState.aceWindowsStatus,
                    uiState.lmuWindowsPitStatus,
                )
            },
        DebugStateCardKey.FLAG_INFO to
            { uiState -> FlagInfoContent(uiState.selectedSimulator, uiState.raceFlags, uiState.aceWindowsFlag) },
        DebugStateCardKey.GAME_PHASE to { uiState -> GamePhaseContent(uiState.raceFlags) },
        DebugStateCardKey.SESSION to { uiState -> SessionContent(uiState.virtualEnergy) },
        DebugStateCardKey.YELLOW_FLAG_STATE to { uiState -> YellowFlagStateContent(uiState.raceFlags) },
        DebugStateCardKey.CURRENT_LAP to { uiState ->
            CurrentLapContent(uiState.selectedSimulator, uiState.lmuWindowsTelemetry, uiState.gt7Ps5Telemetry)
        },
        DebugStateCardKey.SIDE_BY_SIDE_VEHICLES to
            { uiState -> SideBySideVehiclesContent(uiState.vehicleApproach) },
        DebugStateCardKey.BEST_LAP to { uiState ->
            BestLapContent(uiState.selectedSimulator, uiState.lmuWindowsTelemetry, uiState.gt7Ps5Telemetry)
        },
        DebugStateCardKey.TYRE_TEMPERATURE to
            { uiState -> TyreTemperatureContent(uiState.selectedSimulator, uiState.lmuWindowsTelemetry) },
        DebugStateCardKey.TYRE_CARCASS_TEMPERATURE to
            { uiState ->
                TyreCarcassTemperatureContent(
                    uiState.selectedSimulator,
                    uiState.tyreCarcassTemperature,
                    uiState.aceWindowsTyreCarcassTemperature,
                )
            },
        DebugStateCardKey.TYRE_WEAR to
            { uiState -> TyreWearContent(uiState.selectedSimulator, uiState.lmuWindowsTelemetry) },
        DebugStateCardKey.FUEL_CONSUMPTION to { uiState ->
            FuelConsumptionContent(
                uiState.selectedSimulator,
                uiState.virtualEnergy,
                uiState.lmuWindowsTelemetry,
                uiState.gt7Ps5Telemetry,
                uiState.aceWindowsFuel,
            )
        },
        DebugStateCardKey.PIT_TIMING_REMAINING_LAPS to { uiState ->
            PitTimingRemainingLapsContent(
                uiState.selectedSimulator,
                uiState.virtualEnergy,
                uiState.lmuWindowsTelemetry,
            )
        },
    )

@Composable
private fun DebugStateCard(
    cardKey: DebugStateCardKey,
    uiState: DebugStateDetailUiState,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    DetailPaneCard(
        title = stringResource(debugStateCardTitles.getValue(cardKey)),
        modifier = modifier.alpha(if (enabled) 1f else DISABLED_CARD_ALPHA),
        bottomContent = {
            debugStateCardContents.getValue(cardKey)(uiState)
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun DebugStateDetailPanePreview() {
    DebugStateDetailPaneContent(
        uiState = DebugStateDetailUiState(),
        canNavigateBack = true,
        onBack = {},
    )
}
