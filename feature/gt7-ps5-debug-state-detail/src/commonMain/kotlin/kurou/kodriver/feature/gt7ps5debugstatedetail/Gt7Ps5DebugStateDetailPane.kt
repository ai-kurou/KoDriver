package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_best_lap_title
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_current_lap_title
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_fuel_consumption_title
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_simulator_info_title
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_title
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_tyre_temperature_title
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.debug_state_vehicle_class_title
import kurou.kodriver.feature.gt7ps5debugstatedetail.generated.resources.navigate_back
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
        modifier = modifier,
    )
}

@Composable
fun Gt7Ps5DebugStateDetailPaneContent(
    uiState: Gt7Ps5DebugStateDetailUiState,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.testTag(DEBUG_STATE_GRID_TEST_TAG),
            ) {
                items(uiState.cardOrder, key = { it.name }) { cardKey ->
                    DebugStateCard(
                        cardKey = cardKey,
                        uiState = uiState,
                        enabled = cardKey in uiState.enabledCardKeys,
                        modifier = Modifier.padding(KoDriverSpacing.small),
                    )
                }
            }
        }
    }
}

private val debugStateCardTitles: Map<DebugStateCardKey, StringResource> =
    mapOf(
        DebugStateCardKey.SIMULATOR to Res.string.debug_state_simulator_info_title,
        DebugStateCardKey.VEHICLE_CLASS to Res.string.debug_state_vehicle_class_title,
        DebugStateCardKey.CURRENT_LAP to Res.string.debug_state_current_lap_title,
        DebugStateCardKey.BEST_LAP to Res.string.debug_state_best_lap_title,
        DebugStateCardKey.TYRE_TEMPERATURE to Res.string.debug_state_tyre_temperature_title,
        DebugStateCardKey.FUEL_CONSUMPTION to Res.string.debug_state_fuel_consumption_title,
    )

private val debugStateCardContents: Map<DebugStateCardKey, @Composable (Gt7Ps5DebugStateDetailUiState) -> Unit> =
    mapOf(
        DebugStateCardKey.SIMULATOR to { SimulatorInfoContent() },
        DebugStateCardKey.VEHICLE_CLASS to { uiState -> VehicleClassContent(uiState.gt7Ps5.vehicleClass) },
        DebugStateCardKey.CURRENT_LAP to { uiState -> CurrentLapContent(uiState.gt7Ps5.telemetry) },
        DebugStateCardKey.BEST_LAP to { uiState -> BestLapContent(uiState.gt7Ps5.telemetry) },
        DebugStateCardKey.TYRE_TEMPERATURE to { uiState -> TyreTemperatureContent(uiState.gt7Ps5.telemetry) },
        DebugStateCardKey.FUEL_CONSUMPTION to { uiState -> FuelConsumptionContent(uiState.gt7Ps5.telemetry) },
    )

@Composable
private fun DebugStateCard(
    cardKey: DebugStateCardKey,
    uiState: Gt7Ps5DebugStateDetailUiState,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    DebugStateCardFrame(
        cardKey = cardKey,
        title = stringResource(debugStateCardTitles.getValue(cardKey)),
        modifier = modifier.alpha(if (enabled) 1f else DISABLED_CARD_ALPHA),
        content = {
            debugStateCardContents.getValue(cardKey)(uiState)
        },
    )
}

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
