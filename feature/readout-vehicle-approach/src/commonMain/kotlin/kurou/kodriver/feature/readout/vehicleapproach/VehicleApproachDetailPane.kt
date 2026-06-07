package kurou.kodriver.feature.readout.vehicleapproach

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VehicleApproachDetailPane(
    modifier: Modifier = Modifier,
) {
    VehicleApproachDetailPane(
        modifier = modifier,
        viewModel = koinViewModel(),
    )
}

@Composable
internal fun VehicleApproachDetailPane(
    modifier: Modifier = Modifier,
    viewModel: VehicleApproachViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    VehicleApproachDetailPaneContent(uiState = uiState, modifier = modifier)
}

@Composable
internal fun VehicleApproachDetailPaneContent(
    uiState: VehicleApproachUiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        DetailPaneTitle(title = "車両接近")
        DetailPaneDescription(text = "周囲の車両が接近した際に音声でお知らせします。")
    }
}

@Composable
internal fun DetailPaneTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
internal fun DetailPaneDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@Preview
@Composable
private fun VehicleApproachDetailPanePreview() {
    VehicleApproachDetailPaneContent(uiState = VehicleApproachUiState())
}
