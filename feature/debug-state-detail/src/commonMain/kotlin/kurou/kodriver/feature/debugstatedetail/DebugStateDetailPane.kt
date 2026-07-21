package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.debugstatedetail.generated.resources.Res
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_blue
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_full_course_yellow
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_title
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_none
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_red
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_yellow
import kodriver.feature.debugstatedetail.generated.resources.debug_state_simulator_info_title
import kodriver.feature.debugstatedetail.generated.resources.debug_state_simulator_info_unselected
import kodriver.feature.debugstatedetail.generated.resources.debug_state_title
import kodriver.feature.debugstatedetail.generated.resources.navigate_back
import kodriver.feature.debugstatedetail.generated.resources.simulator_name_gt7_ps5
import kodriver.feature.debugstatedetail.generated.resources.simulator_name_lmu
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.Simulator
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
        modifier = modifier,
    )
}

@Composable
internal fun DebugStateDetailPaneContent(
    uiState: DebugStateDetailUiState,
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
            LazyVerticalGrid(columns = GridCells.Fixed(columns)) {
                item {
                    DetailPaneCard(
                        title = stringResource(Res.string.debug_state_simulator_info_title),
                        modifier = Modifier.padding(8.dp),
                        bottomContent = {
                            SimulatorInfoContent(uiState.selectedSimulator)
                        },
                    )
                }
                item {
                    DetailPaneCard(
                        title = stringResource(Res.string.debug_state_flag_info_title),
                        modifier = Modifier.padding(8.dp),
                        bottomContent = {
                            FlagInfoContent(uiState.raceFlags)
                        },
                    )
                }
            }
        }
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

@Preview(showBackground = true)
@Composable
private fun DebugStateDetailPanePreview() {
    DebugStateDetailPaneContent(
        uiState = DebugStateDetailUiState(),
        canNavigateBack = true,
        onBack = {},
    )
}
