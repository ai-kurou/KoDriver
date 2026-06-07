package kurou.kodriver.feature.readout.vehicleapproach

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.DetailPaneTitle
import kurou.kodriver.core.designsystem.ThresholdSlider
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
    VehicleApproachDetailPaneContent(
        uiState = uiState,
        onLongitudinalThresholdChanged = viewModel::onLongitudinalThresholdChanged,
        onLateralThresholdChanged = viewModel::onLateralThresholdChanged,
        modifier = modifier,
    )
}

@Composable
internal fun VehicleApproachDetailPaneContent(
    uiState: VehicleApproachUiState,
    onLongitudinalThresholdChanged: (Double) -> Unit = {},
    onLateralThresholdChanged: (Double) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        DetailPaneTitle(title = "車両接近")
        DetailPaneDescription(text = "周囲の車両が接近した際に音声でお知らせします。")
        DetailPaneSubtitle(text = "閾値設定")
        ThresholdSlider(
            value = uiState.longitudinalThresholdMeters.toFloat(),
            valueRange = 0.1f..2f,
            labelFormatter = { "縦方向: ${"%.1f".format(it)} m" },
            onValueChangeFinished = { onLongitudinalThresholdChanged(it.toDouble()) },
        )
        ThresholdSlider(
            value = uiState.lateralThresholdMeters.toFloat(),
            valueRange = 0.5f..5f,
            labelFormatter = { "横方向: ${"%.1f".format(it)} m" },
            onValueChangeFinished = { onLateralThresholdChanged(it.toDouble()) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VehicleApproachDetailPanePreview() {
    VehicleApproachDetailPaneContent(uiState = VehicleApproachUiState())
}
