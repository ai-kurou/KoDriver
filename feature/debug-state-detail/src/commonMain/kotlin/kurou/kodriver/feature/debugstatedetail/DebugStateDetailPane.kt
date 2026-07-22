package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.debugstatedetail.generated.resources.Res
import kodriver.feature.debugstatedetail.generated.resources.debug_state_best_lap_title
import kodriver.feature.debugstatedetail.generated.resources.debug_state_current_lap_title
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_blue
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_full_course_yellow
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_title
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_none
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_red
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_yellow
import kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_title
import kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_countdown
import kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_formation
import kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_full_course_yellow
import kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_garage
import kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_green_flag
import kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_grid_walk
import kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_paused
import kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_session_over
import kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_session_stopped
import kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_title
import kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_unknown
import kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_warm_up
import kodriver.feature.debugstatedetail.generated.resources.debug_state_session_practice
import kodriver.feature.debugstatedetail.generated.resources.debug_state_session_qualifying
import kodriver.feature.debugstatedetail.generated.resources.debug_state_session_race
import kodriver.feature.debugstatedetail.generated.resources.debug_state_session_test_day
import kodriver.feature.debugstatedetail.generated.resources.debug_state_session_title
import kodriver.feature.debugstatedetail.generated.resources.debug_state_session_unknown
import kodriver.feature.debugstatedetail.generated.resources.debug_state_session_warmup
import kodriver.feature.debugstatedetail.generated.resources.debug_state_side_by_side_left
import kodriver.feature.debugstatedetail.generated.resources.debug_state_side_by_side_none
import kodriver.feature.debugstatedetail.generated.resources.debug_state_side_by_side_right
import kodriver.feature.debugstatedetail.generated.resources.debug_state_side_by_side_title
import kodriver.feature.debugstatedetail.generated.resources.debug_state_simulator_info_title
import kodriver.feature.debugstatedetail.generated.resources.debug_state_simulator_info_unselected
import kodriver.feature.debugstatedetail.generated.resources.debug_state_title
import kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_temperature_title
import kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_invalid
import kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_last_lap
import kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_none
import kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_pending
import kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_pit_closed
import kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_pit_lead_lap
import kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_pit_open
import kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_race_halt
import kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_resume
import kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_title
import kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_unknown
import kodriver.feature.debugstatedetail.generated.resources.navigate_back
import kodriver.feature.debugstatedetail.generated.resources.simulator_name_gt7_ps5
import kodriver.feature.debugstatedetail.generated.resources.simulator_name_lmu
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.domain.model.Simulator
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import kotlin.math.round

private val NARROW_WIDTH_UPPER_BOUND = 400.dp
private val MEDIUM_WIDTH_UPPER_BOUND = 700.dp

internal fun calculateDebugStateColumns(maxWidth: Dp): Int = when {
    maxWidth < NARROW_WIDTH_UPPER_BOUND -> 1
    maxWidth < MEDIUM_WIDTH_UPPER_BOUND -> 2
    else -> 3
}

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
internal fun DebugStateDetailPaneContent(
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
            val reorderableState = rememberReorderableLazyGridState(gridState) { from, to ->
                onMoveCard(from.index, to.index)
            }
            LazyVerticalGrid(columns = GridCells.Fixed(columns), state = gridState) {
                items(uiState.cardOrder, key = { it.name }) { cardKey ->
                    ReorderableItem(reorderableState, key = cardKey.name) {
                        DebugStateCard(
                            cardKey = cardKey,
                            uiState = uiState,
                            modifier = Modifier.padding(8.dp).longPressDraggableHandle(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugStateCard(
    cardKey: DebugStateCardKey,
    uiState: DebugStateDetailUiState,
    modifier: Modifier = Modifier,
) {
    when (cardKey) {
        DebugStateCardKey.SIMULATOR -> DetailPaneCard(
            title = stringResource(Res.string.debug_state_simulator_info_title),
            modifier = modifier,
            bottomContent = {
                SimulatorInfoContent(uiState.selectedSimulator)
            },
        )
        DebugStateCardKey.FLAG_INFO -> DetailPaneCard(
            title = stringResource(Res.string.debug_state_flag_info_title),
            modifier = modifier,
            bottomContent = {
                FlagInfoContent(uiState.raceFlags)
            },
        )
        DebugStateCardKey.GAME_PHASE -> DetailPaneCard(
            title = stringResource(Res.string.debug_state_game_phase_title),
            modifier = modifier,
            bottomContent = {
                GamePhaseContent(uiState.raceFlags)
            },
        )
        DebugStateCardKey.SESSION -> DetailPaneCard(
            title = stringResource(Res.string.debug_state_session_title),
            modifier = modifier,
            bottomContent = {
                SessionContent(uiState.virtualEnergy)
            },
        )
        DebugStateCardKey.YELLOW_FLAG_STATE -> DetailPaneCard(
            title = stringResource(Res.string.debug_state_yellow_flag_state_title),
            modifier = modifier,
            bottomContent = {
                YellowFlagStateContent(uiState.raceFlags)
            },
        )
        DebugStateCardKey.CURRENT_LAP -> DetailPaneCard(
            title = stringResource(Res.string.debug_state_current_lap_title),
            modifier = modifier,
            bottomContent = {
                CurrentLapContent(uiState.selectedSimulator, uiState.lmuWindowsTelemetry, uiState.gt7Ps5Telemetry)
            },
        )
        DebugStateCardKey.SIDE_BY_SIDE_VEHICLES -> DetailPaneCard(
            title = stringResource(Res.string.debug_state_side_by_side_title),
            modifier = modifier,
            bottomContent = {
                SideBySideVehiclesContent(uiState.vehicleApproach)
            },
        )
        DebugStateCardKey.BEST_LAP -> DetailPaneCard(
            title = stringResource(Res.string.debug_state_best_lap_title),
            modifier = modifier,
            bottomContent = {
                BestLapContent(uiState.selectedSimulator, uiState.lmuWindowsTelemetry, uiState.gt7Ps5Telemetry)
            },
        )
        DebugStateCardKey.TYRE_TEMPERATURE -> DetailPaneCard(
            title = stringResource(Res.string.debug_state_tyre_temperature_title),
            modifier = modifier,
            bottomContent = {
                TyreTemperatureContent(uiState.selectedSimulator, uiState.lmuWindowsTelemetry)
            },
        )
        DebugStateCardKey.FUEL_CONSUMPTION -> DetailPaneCard(
            title = stringResource(Res.string.debug_state_fuel_consumption_title),
            modifier = modifier,
            bottomContent = {
                FuelConsumptionContent(
                    uiState.selectedSimulator,
                    uiState.virtualEnergy,
                    uiState.lmuWindowsTelemetry,
                    uiState.gt7Ps5Telemetry,
                )
            },
        )
    }
}

@Composable
private fun simulatorDisplayName(simulator: Simulator): String = when (simulator) {
    is Simulator.LmuWindows -> stringResource(Res.string.simulator_name_lmu)
    is Simulator.Gt7Ps5 -> stringResource(Res.string.simulator_name_gt7_ps5)
}

@Composable
private fun SimulatorInfoContent(selectedSimulator: Simulator?) {
    Text(
        text = selectedSimulator
            ?.let { simulatorDisplayName(it) }
            ?: stringResource(Res.string.debug_state_simulator_info_unselected),
    )
}

internal enum class ActiveRaceFlag(val labelRes: StringResource) {
    BLUE(Res.string.debug_state_flag_blue),
    YELLOW(Res.string.debug_state_flag_yellow),
    FULL_COURSE_YELLOW(Res.string.debug_state_flag_full_course_yellow),
    RED(Res.string.debug_state_flag_red),
}

internal fun determineActiveRaceFlags(raceFlags: LmuWindowsRaceFlagsData): List<ActiveRaceFlag> = buildList {
    if (raceFlags.playerFlag == PrimaryFlag.BLUE) add(ActiveRaceFlag.BLUE)
    if (raceFlags.playerUnderYellow || raceFlags.sectorFlags.any { it == SectorFlagState.YELLOW }) {
        add(ActiveRaceFlag.YELLOW)
    }
    if (raceFlags.gamePhase == SessionPhase.FULL_COURSE_YELLOW) add(ActiveRaceFlag.FULL_COURSE_YELLOW)
    if (raceFlags.gamePhase == SessionPhase.RED_FLAG) add(ActiveRaceFlag.RED)
}

@Composable
private fun FlagInfoContent(raceFlags: LmuWindowsRaceFlagsData?) {
    if (raceFlags == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    val activeFlags = determineActiveRaceFlags(raceFlags)
    Column {
        if (activeFlags.isEmpty()) {
            Text(text = stringResource(Res.string.debug_state_flag_none))
        } else {
            activeFlags.forEach { flag ->
                Text(text = stringResource(flag.labelRes))
            }
        }
    }
}

@Composable
private fun gamePhaseDisplayName(gamePhase: SessionPhase): String = when (gamePhase) {
    SessionPhase.GARAGE -> stringResource(Res.string.debug_state_game_phase_garage)
    SessionPhase.WARM_UP -> stringResource(Res.string.debug_state_game_phase_warm_up)
    SessionPhase.GRID_WALK -> stringResource(Res.string.debug_state_game_phase_grid_walk)
    SessionPhase.FORMATION -> stringResource(Res.string.debug_state_game_phase_formation)
    SessionPhase.COUNTDOWN -> stringResource(Res.string.debug_state_game_phase_countdown)
    SessionPhase.GREEN_FLAG -> stringResource(Res.string.debug_state_game_phase_green_flag)
    SessionPhase.FULL_COURSE_YELLOW -> stringResource(Res.string.debug_state_game_phase_full_course_yellow)
    SessionPhase.RED_FLAG -> stringResource(Res.string.debug_state_game_phase_session_stopped)
    SessionPhase.SESSION_OVER -> stringResource(Res.string.debug_state_game_phase_session_over)
    SessionPhase.PAUSED_OR_HEARTBEAT -> stringResource(Res.string.debug_state_game_phase_paused)
    SessionPhase.UNKNOWN -> stringResource(Res.string.debug_state_game_phase_unknown)
}

@Composable
private fun GamePhaseContent(raceFlags: LmuWindowsRaceFlagsData?) {
    if (raceFlags == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Text(text = gamePhaseDisplayName(raceFlags.gamePhase))
}

@Composable
private fun sessionDisplayName(session: Int): String = when (session) {
    0 -> stringResource(Res.string.debug_state_session_test_day)
    in 1..4 -> stringResource(Res.string.debug_state_session_practice)
    in 5..8 -> stringResource(Res.string.debug_state_session_qualifying)
    9 -> stringResource(Res.string.debug_state_session_warmup)
    in 10..13 -> stringResource(Res.string.debug_state_session_race)
    else -> stringResource(Res.string.debug_state_session_unknown)
}

@Composable
private fun SessionContent(virtualEnergy: LmuWindowsVirtualEnergyData?) {
    if (virtualEnergy == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Text(text = sessionDisplayName(virtualEnergy.session))
}

@Composable
private fun yellowFlagStateDisplayName(yellowFlagState: SessionYellowFlagState): String = when (yellowFlagState) {
    SessionYellowFlagState.INVALID -> stringResource(Res.string.debug_state_yellow_flag_state_invalid)
    SessionYellowFlagState.NONE -> stringResource(Res.string.debug_state_yellow_flag_state_none)
    SessionYellowFlagState.PENDING -> stringResource(Res.string.debug_state_yellow_flag_state_pending)
    SessionYellowFlagState.PIT_CLOSED -> stringResource(Res.string.debug_state_yellow_flag_state_pit_closed)
    SessionYellowFlagState.PIT_LEAD_LAP -> stringResource(Res.string.debug_state_yellow_flag_state_pit_lead_lap)
    SessionYellowFlagState.PIT_OPEN -> stringResource(Res.string.debug_state_yellow_flag_state_pit_open)
    SessionYellowFlagState.LAST_LAP -> stringResource(Res.string.debug_state_yellow_flag_state_last_lap)
    SessionYellowFlagState.RESUME -> stringResource(Res.string.debug_state_yellow_flag_state_resume)
    SessionYellowFlagState.RACE_HALT -> stringResource(Res.string.debug_state_yellow_flag_state_race_halt)
    SessionYellowFlagState.UNKNOWN -> stringResource(Res.string.debug_state_yellow_flag_state_unknown)
}

@Composable
private fun YellowFlagStateContent(raceFlags: LmuWindowsRaceFlagsData?) {
    if (raceFlags == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Text(text = yellowFlagStateDisplayName(raceFlags.yellowFlagState))
}

@Composable
private fun CurrentLapContent(
    selectedSimulator: Simulator?,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
    gt7Ps5Telemetry: Gt7Ps5TelemetryData?,
) {
    val currentLap = when (selectedSimulator) {
        is Simulator.LmuWindows -> lmuWindowsTelemetry?.timing?.currentLap
        is Simulator.Gt7Ps5 -> gt7Ps5Telemetry?.lapCount
        null -> null
    }
    Text(
        text = currentLap?.toString() ?: stringResource(Res.string.debug_state_flag_info_unavailable),
    )
}

@Composable
private fun BestLapContent(
    selectedSimulator: Simulator?,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
    gt7Ps5Telemetry: Gt7Ps5TelemetryData?,
) {
    val bestLapTimeMs = when (selectedSimulator) {
        is Simulator.LmuWindows -> lmuWindowsTelemetry?.timing?.bestLapTimeMs
        is Simulator.Gt7Ps5 -> gt7Ps5Telemetry?.bestLapTimeMs?.toLong()
        null -> null
    }
    Text(
        text = bestLapTimeMs?.takeIf { it > 0L }?.let { formatLapTimeMs(it) }
            ?: stringResource(Res.string.debug_state_flag_info_unavailable),
    )
}

private fun formatMeters(value: Double): String {
    val rounded = round(value * 10) / 10
    return rounded.toString()
}

@Composable
private fun SideBySideVehiclesContent(vehicleApproach: LmuWindowsVehicleApproachData?) {
    if (vehicleApproach == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Column {
        if (!vehicleApproach.isSideBySideLeft && !vehicleApproach.isSideBySideRight) {
            Text(text = stringResource(Res.string.debug_state_side_by_side_none))
        } else {
            if (vehicleApproach.isSideBySideLeft) {
                Text(
                    text = stringResource(
                        Res.string.debug_state_side_by_side_left,
                        formatMeters(vehicleApproach.lateralDistanceLeftMeters),
                    ),
                )
            }
            if (vehicleApproach.isSideBySideRight) {
                Text(
                    text = stringResource(
                        Res.string.debug_state_side_by_side_right,
                        formatMeters(vehicleApproach.lateralDistanceRightMeters),
                    ),
                )
            }
        }
    }
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
