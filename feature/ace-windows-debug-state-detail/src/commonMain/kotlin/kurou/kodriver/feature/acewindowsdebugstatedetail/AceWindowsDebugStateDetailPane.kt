package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DebugReadoutRequestCard
import kurou.kodriver.core.designsystem.DebugStateCard
import kurou.kodriver.core.designsystem.DebugStateReorderableGrid
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ACE_WINDOWS_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_best_lap_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_car_gap_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_flag_info_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_fuel_consumption_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_side_by_side_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_simulator_info_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_tyre_carcass_temperature_title
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_vehicle_location_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
        onBack = onBack,
        modifier = modifier,
    ) {
        DebugStateReorderableGrid(
            cardOrder = uiState.cardOrder,
            fixedItemCount = 1,
            fixedItems = {
                item(key = DEBUG_READOUT_REQUEST_ITEM_KEY, span = { GridItemSpan(maxLineSpan) }) {
                    DebugReadoutRequestCard(
                        readoutItemKeyValues =
                            ACE_WINDOWS_READOUT_ITEM_KEYS
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
            },
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

private val debugStateCardTitles: Map<AceWindowsDebugStateCardKey, StringResource> =
    mapOf(
        AceWindowsDebugStateCardKey.SIMULATOR to Res.string.debug_state_simulator_info_title,
        AceWindowsDebugStateCardKey.VEHICLE_LOCATION to Res.string.debug_state_vehicle_location_title,
        AceWindowsDebugStateCardKey.FLAG_INFO to Res.string.debug_state_flag_info_title,
        AceWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES to Res.string.debug_state_side_by_side_title,
        AceWindowsDebugStateCardKey.BEST_LAP to Res.string.debug_state_best_lap_title,
        AceWindowsDebugStateCardKey.CAR_GAP to Res.string.debug_state_car_gap_title,
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
        AceWindowsDebugStateCardKey.CAR_GAP to { uiState ->
            CarGapContent(uiState.aceWindows.gap, uiState.aceWindows.overallPosition)
        },
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
