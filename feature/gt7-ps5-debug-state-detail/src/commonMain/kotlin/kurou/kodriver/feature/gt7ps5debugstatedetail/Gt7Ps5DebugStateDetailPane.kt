package kurou.kodriver.feature.gt7ps5debugstatedetail

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
import kurou.kodriver.domain.model.GT7_PS5_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.ReadoutItemKey
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
        DebugStateReorderableGrid(
            cardOrder = uiState.cardOrder,
            fixedItemCount = 1,
            fixedItems = {
                item(key = DEBUG_READOUT_REQUEST_ITEM_KEY, span = { GridItemSpan(maxLineSpan) }) {
                    DebugReadoutRequestCard(
                        readoutItemKeyValues =
                            GT7_PS5_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>().map {
                                it.value
                            },
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
