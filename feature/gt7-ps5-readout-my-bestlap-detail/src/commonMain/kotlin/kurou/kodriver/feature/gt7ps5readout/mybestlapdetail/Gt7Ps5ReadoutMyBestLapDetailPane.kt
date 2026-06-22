package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.gt7ps5readout.mybestlapdetail.generated.resources.Res
import kodriver.feature.gt7ps5readout.mybestlapdetail.generated.resources.my_best_lap_description
import kodriver.feature.gt7ps5readout.mybestlapdetail.generated.resources.my_best_lap_enabled
import kodriver.feature.gt7ps5readout.mybestlapdetail.generated.resources.my_best_lap_switch_subtitle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Gt7Ps5ReadoutMyBestLapDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: Gt7Ps5ReadoutMyBestLapDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Gt7Ps5ReadoutMyBestLapDetailPaneContent(
        uiState = uiState,
        onEnabledChanged = viewModel::onEnabledChanged,
        modifier = modifier,
    )
}

@Composable
internal fun Gt7Ps5ReadoutMyBestLapDetailPaneContent(
    uiState: Gt7Ps5ReadoutMyBestLapDetailUiState,
    onEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(text = stringResource(Res.string.my_best_lap_description))
        DetailPaneSubtitle(text = stringResource(Res.string.my_best_lap_switch_subtitle))
        DetailPaneCard(
            title = stringResource(Res.string.my_best_lap_enabled),
            checked = uiState.enabled,
            chipLabels = emptyList(),
            selectedChipLabels = emptySet(),
            onCheckedChange = onEnabledChanged,
            onChipClick = {},
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Gt7Ps5ReadoutMyBestLapDetailPanePreview() {
    Gt7Ps5ReadoutMyBestLapDetailPaneContent(
        uiState = Gt7Ps5ReadoutMyBestLapDetailUiState(enabled = true),
        onEnabledChanged = {},
    )
}
