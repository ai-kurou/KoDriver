package kurou.kodriver.feature.gt7ps5debugstatedetail

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
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_best_lap_title
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_current_lap_title
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_fuel_consumption_title
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_simulator_info_title
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_title
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_tyre_temperature_title
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_vehicle_class_title
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
 * GT7 PS5 専用のデバッグ状態画面を表示する Composable。
 */
@Composable
fun Gt7Ps5DebugStateDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: Gt7Ps5DebugStateDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Gt7Ps5DebugStateDetailPaneContent(
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
internal fun Gt7Ps5DebugStateDetailPaneContent(
    uiState: Gt7Ps5DebugStateDetailUiState,
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
                    readoutItemKeyValues = ReadoutItemKey.topLevelEntriesOf(Simulator.Gt7Ps5).map { it.value },
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
                                titleOf = debugStateCardTitles::getValue,
                                contentOf = debugStateCardContents::getValue,
                                frame = ::DebugStateCardFrame,
                                modifier = Modifier.padding(KoDriverSpacing.small).longPressDraggableHandle(),
                            )
                        }
                    }
                }
            }
        }
    }
}

private val debugStateCardTitles: Map<Gt7Ps5DebugStateCardKey, StringResource> =
    mapOf(
        Gt7Ps5DebugStateCardKey.SIMULATOR to Res.string.debug_state_simulator_info_title,
        Gt7Ps5DebugStateCardKey.VEHICLE_CLASS to Res.string.debug_state_vehicle_class_title,
        Gt7Ps5DebugStateCardKey.CURRENT_LAP to Res.string.debug_state_current_lap_title,
        Gt7Ps5DebugStateCardKey.BEST_LAP to Res.string.debug_state_best_lap_title,
        Gt7Ps5DebugStateCardKey.TYRE_TEMPERATURE to Res.string.debug_state_tyre_temperature_title,
        Gt7Ps5DebugStateCardKey.FUEL_CONSUMPTION to Res.string.debug_state_fuel_consumption_title,
    )

private val debugStateCardContents: Map<Gt7Ps5DebugStateCardKey, @Composable (Gt7Ps5DebugStateDetailUiState) -> Unit> =
    mapOf(
        Gt7Ps5DebugStateCardKey.SIMULATOR to { SimulatorInfoContent() },
        Gt7Ps5DebugStateCardKey.VEHICLE_CLASS to { uiState -> VehicleClassContent(uiState.gt7Ps5.vehicleClass) },
        Gt7Ps5DebugStateCardKey.CURRENT_LAP to { uiState -> CurrentLapContent(uiState.gt7Ps5.telemetry) },
        Gt7Ps5DebugStateCardKey.BEST_LAP to { uiState -> BestLapContent(uiState.gt7Ps5.telemetry) },
        Gt7Ps5DebugStateCardKey.TYRE_TEMPERATURE to { uiState -> TyreTemperatureContent(uiState.gt7Ps5.telemetry) },
        Gt7Ps5DebugStateCardKey.FUEL_CONSUMPTION to { uiState -> FuelConsumptionContent(uiState.gt7Ps5.telemetry) },
    )

@Preview(showBackground = true)
@Composable
private fun Gt7Ps5DebugStateDetailPanePreview() {
    KoDriverTheme {
        Gt7Ps5DebugStateDetailPaneContent(
            uiState = Gt7Ps5DebugStateDetailUiState(),
            canNavigateBack = true,
            onBack = {},
        )
    }
}
