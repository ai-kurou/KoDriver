package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.LMU_WINDOWS_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_best_lap_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_brake_temperature_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_brake_wear_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_current_lap_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_flag_info_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_fuel_consumption_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_game_phase_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_pit_timing_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_session_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_side_by_side_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_simulator_info_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_tyre_carcass_temperature_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_tyre_temperature_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_tyre_wear_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_vehicle_class_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_vehicle_damage_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_vehicle_location_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_virtual_energy_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_yellow_flag_state_title
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
 * LMU Windows 専用のデバッグ状態画面を表示する Composable。
 */
@Composable
fun LmuWindowsDebugStateDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: LmuWindowsDebugStateDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsDebugStateDetailPaneContent(
        uiState = uiState,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        onMoveCard = viewModel::moveCard,
        onReadoutItemSelected = viewModel::selectReadoutItem,
        onDebugReadoutTextChange = viewModel::updateDebugReadoutText,
        onRequestReadout = viewModel::requestReadout,
        onHotkeySelected = viewModel::selectHotkey,
        onCheckHotkey = viewModel::checkHotkey,
        modifier = modifier,
    )
}

@Composable
fun LmuWindowsDebugStateDetailPaneContent(
    uiState: LmuWindowsDebugStateDetailUiState,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    onMoveCard: (Int, Int) -> Unit = { _, _ -> },
    onReadoutItemSelected: (String) -> Unit = {},
    onDebugReadoutTextChange: (String) -> Unit = {},
    onRequestReadout: () -> Unit = {},
    onHotkeySelected: (Int) -> Unit = {},
    onCheckHotkey: () -> Unit = {},
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
            val showHotkey = uiState.readoutHotkey.registrationState != HotkeyRegistrationState.Unsupported
            val fixedItemCount = if (showHotkey) 2 else 1
            val reorderableState =
                rememberReorderableLazyGridState(gridState) { from, to ->
                    if (from.index >= fixedItemCount && to.index >= fixedItemCount) {
                        onMoveCard(from.index - fixedItemCount, to.index - fixedItemCount)
                    }
                }
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = gridState,
                modifier = Modifier.testTag(DEBUG_STATE_GRID_TEST_TAG),
            ) {
                item(key = DEBUG_READOUT_REQUEST_ITEM_KEY, span = { GridItemSpan(maxLineSpan) }) {
                    DebugReadoutRequestCard(
                        readoutItemKeyValues =
                            LMU_WINDOWS_READOUT_ITEM_KEYS
                                .filterIsInstance<ReadoutItemKey.TopLevel>()
                                .map { it.value },
                        selectedReadoutItemKeyValue = uiState.selectedReadoutItemKey.value,
                        text = uiState.debugReadoutText,
                        onReadoutItemSelected = onReadoutItemSelected,
                        onTextChange = onDebugReadoutTextChange,
                        onReadout = onRequestReadout,
                        modifier = Modifier.padding(KoDriverSpacing.small),
                    )
                }
                if (showHotkey) {
                    item(key = READOUT_HOTKEY_ITEM_KEY, span = { GridItemSpan(maxLineSpan) }) {
                        ReadoutHotkeyCard(
                            state = uiState.readoutHotkey,
                            onKeySelected = onHotkeySelected,
                            onCheck = onCheckHotkey,
                            modifier = Modifier.padding(KoDriverSpacing.small),
                        )
                    }
                }
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

private const val DEBUG_READOUT_REQUEST_ITEM_KEY = "debugReadoutRequest"
private const val READOUT_HOTKEY_ITEM_KEY = "readoutHotkey"

private val debugStateCardTitles: Map<LmuWindowsDebugStateCardKey, StringResource> =
    mapOf(
        LmuWindowsDebugStateCardKey.SIMULATOR to Res.string.debug_state_simulator_info_title,
        LmuWindowsDebugStateCardKey.VEHICLE_CLASS to Res.string.debug_state_vehicle_class_title,
        LmuWindowsDebugStateCardKey.VEHICLE_LOCATION to Res.string.debug_state_vehicle_location_title,
        LmuWindowsDebugStateCardKey.FLAG_INFO to Res.string.debug_state_flag_info_title,
        LmuWindowsDebugStateCardKey.GAME_PHASE to Res.string.debug_state_game_phase_title,
        LmuWindowsDebugStateCardKey.SESSION to Res.string.debug_state_session_title,
        LmuWindowsDebugStateCardKey.YELLOW_FLAG_STATE to Res.string.debug_state_yellow_flag_state_title,
        LmuWindowsDebugStateCardKey.CURRENT_LAP to Res.string.debug_state_current_lap_title,
        LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES to Res.string.debug_state_side_by_side_title,
        LmuWindowsDebugStateCardKey.BEST_LAP to Res.string.debug_state_best_lap_title,
        LmuWindowsDebugStateCardKey.TYRE_TEMPERATURE to Res.string.debug_state_tyre_temperature_title,
        LmuWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE to Res.string.debug_state_tyre_carcass_temperature_title,
        LmuWindowsDebugStateCardKey.BRAKE_TEMPERATURE to Res.string.debug_state_brake_temperature_title,
        LmuWindowsDebugStateCardKey.BRAKE_WEAR to Res.string.debug_state_brake_wear_title,
        LmuWindowsDebugStateCardKey.TYRE_WEAR to Res.string.debug_state_tyre_wear_title,
        LmuWindowsDebugStateCardKey.FUEL_CONSUMPTION to Res.string.debug_state_fuel_consumption_title,
        LmuWindowsDebugStateCardKey.VIRTUAL_ENERGY to Res.string.debug_state_virtual_energy_title,
        LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS to Res.string.debug_state_pit_timing_title,
        LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE to Res.string.debug_state_vehicle_damage_title,
    )

private val debugStateCardContents:
    Map<LmuWindowsDebugStateCardKey, @Composable (LmuWindowsDebugStateDetailUiState) -> Unit> =
    mapOf(
        LmuWindowsDebugStateCardKey.SIMULATOR to { SimulatorInfoContent() },
        LmuWindowsDebugStateCardKey.VEHICLE_CLASS to
            { uiState ->
                VehicleClassContent(uiState.lmuWindows.vehicleClass)
            },
        LmuWindowsDebugStateCardKey.VEHICLE_LOCATION to
            { uiState ->
                VehicleLocationContent(uiState.lmuWindows.pitStatus)
            },
        LmuWindowsDebugStateCardKey.FLAG_INFO to
            { uiState ->
                FlagInfoContent(uiState.lmuWindows.raceFlags)
            },
        LmuWindowsDebugStateCardKey.GAME_PHASE to { uiState -> GamePhaseContent(uiState.lmuWindows.raceFlags) },
        LmuWindowsDebugStateCardKey.SESSION to { uiState -> SessionContent(uiState.lmuWindows.virtualEnergy) },
        LmuWindowsDebugStateCardKey.YELLOW_FLAG_STATE to { uiState ->
            YellowFlagStateContent(uiState.lmuWindows.raceFlags)
        },
        LmuWindowsDebugStateCardKey.CURRENT_LAP to { uiState ->
            CurrentLapContent(uiState.lmuWindows.telemetry)
        },
        LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES to
            { uiState ->
                SideBySideVehiclesContent(
                    uiState.lmuWindows.vehicleApproach,
                    uiState.lmuWindows.sideBySideDurations,
                )
            },
        LmuWindowsDebugStateCardKey.BEST_LAP to { uiState ->
            BestLapContent(uiState.lmuWindows.telemetry)
        },
        LmuWindowsDebugStateCardKey.TYRE_TEMPERATURE to
            { uiState ->
                TyreTemperatureContent(uiState.lmuWindows.telemetry)
            },
        LmuWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE to
            { uiState ->
                TyreCarcassTemperatureContent(uiState.lmuWindows.tyreCarcassTemperature)
            },
        LmuWindowsDebugStateCardKey.BRAKE_TEMPERATURE to
            { uiState -> BrakeTemperatureContent(uiState.lmuWindows.brakeTemperature) },
        LmuWindowsDebugStateCardKey.BRAKE_WEAR to
            { uiState -> BrakeWearContent(uiState.lmuWindows.brakeWear) },
        LmuWindowsDebugStateCardKey.TYRE_WEAR to
            { uiState -> TyreWearContent(uiState.lmuWindows.telemetry) },
        LmuWindowsDebugStateCardKey.FUEL_CONSUMPTION to { uiState ->
            FuelContent(uiState.lmuWindows.telemetry)
        },
        LmuWindowsDebugStateCardKey.VIRTUAL_ENERGY to { uiState ->
            VirtualEnergyContent(
                uiState.lmuWindows.virtualEnergy,
                uiState.lmuWindows.telemetry,
            )
        },
        LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS to { uiState ->
            PitTimingRemainingLapsContent(
                uiState.lmuWindows.virtualEnergy,
                uiState.lmuWindows.telemetry,
            )
        },
        LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE to { uiState ->
            VehicleDamageContent(uiState.lmuWindows.vehicleDamage, uiState.lmuWindows.tyreDetached)
        },
    )

@Preview(showBackground = true)
@Composable
private fun LmuWindowsDebugStateDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsDebugStateDetailPaneContent(
            uiState = LmuWindowsDebugStateDetailUiState(),
            canNavigateBack = true,
            onBack = {},
        )
    }
}
