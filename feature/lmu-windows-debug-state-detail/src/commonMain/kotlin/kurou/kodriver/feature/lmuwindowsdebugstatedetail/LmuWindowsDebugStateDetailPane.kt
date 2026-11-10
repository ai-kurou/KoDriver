package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DebugReadoutRequestCard
import kurou.kodriver.core.designsystem.DebugStateCard
import kurou.kodriver.core.designsystem.DebugStateFixedCard
import kurou.kodriver.core.designsystem.DebugStateReorderableGrid
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.LMU_WINDOWS_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_best_lap_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_brake_temperature_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_brake_wear_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_car_gap_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_class_car_gap_title
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
        val showHotkey = uiState.readoutHotkey.registrationState != HotkeyRegistrationState.Unsupported
        DebugStateReorderableGrid(
            cardOrder = uiState.cardOrder,
            fixedCards =
                listOfNotNull(
                    DebugStateFixedCard(DEBUG_READOUT_REQUEST_ITEM_KEY) { cardModifier ->
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
                            modifier = cardModifier,
                        )
                    },
                    DebugStateFixedCard(READOUT_HOTKEY_ITEM_KEY) { cardModifier ->
                        ReadoutHotkeyCard(
                            state = uiState.readoutHotkey,
                            onKeySelected = onHotkeySelected,
                            onCheck = onCheckHotkey,
                            modifier = cardModifier,
                        )
                    }.takeIf { showHotkey },
                ),
            onMoveCard = onMoveCard,
            card = { cardKey, cardModifier ->
                DebugStateCard(
                    cardKey = cardKey,
                    uiState = uiState,
                    enabled = cardKey in uiState.enabledCardKeys,
                    titles = debugStateCardTitles,
                    contents = debugStateCardContents,
                    frame = { title, frameModifier, content ->
                        DebugStateCardFrame(
                            cardKey = cardKey,
                            title = title,
                            modifier = frameModifier,
                            content = content,
                        )
                    },
                    modifier = cardModifier,
                )
            },
        )
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
        LmuWindowsDebugStateCardKey.CAR_GAP to Res.string.debug_state_car_gap_title,
        LmuWindowsDebugStateCardKey.CLASS_CAR_GAP to Res.string.debug_state_class_car_gap_title,
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
        LmuWindowsDebugStateCardKey.CAR_GAP to { uiState ->
            CarGapContent(uiState.lmuWindows.carGap)
        },
        LmuWindowsDebugStateCardKey.CLASS_CAR_GAP to { uiState ->
            ClassCarGapContent(uiState.lmuWindows.classCarGap)
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
                uiState.lmuWindows.virtualEnergyBaseline,
            )
        },
        LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS to { uiState ->
            PitTimingRemainingLapsContent(
                uiState.lmuWindows.virtualEnergy,
                uiState.lmuWindows.telemetry,
                uiState.lmuWindows.virtualEnergyBaseline,
                uiState.lmuWindows.tyreWearBaseline,
                uiState.lmuWindows.fuelBaseline,
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
