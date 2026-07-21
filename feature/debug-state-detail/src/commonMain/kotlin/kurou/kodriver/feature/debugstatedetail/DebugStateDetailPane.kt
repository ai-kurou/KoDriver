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
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_title
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kodriver.feature.debugstatedetail.generated.resources.debug_state_title
import kodriver.feature.debugstatedetail.generated.resources.navigate_back
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
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
private fun FlagInfoContent(raceFlags: LmuWindowsRaceFlagsData?) {
    if (raceFlags == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Column {
        Text(text = "gamePhase: ${raceFlags.gamePhase}")
        Text(text = "yellowFlagState: ${raceFlags.yellowFlagState}")
        Text(text = "sectorFlags: ${raceFlags.sectorFlags}")
        Text(text = "startLight: ${raceFlags.startLight}")
        Text(text = "numRedLights: ${raceFlags.numRedLights}")
        Text(text = "playerFlag: ${raceFlags.playerFlag}")
        Text(text = "playerUnderYellow: ${raceFlags.playerUnderYellow}")
        Text(text = "playerCountLapFlag: ${raceFlags.playerCountLapFlag}")
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
