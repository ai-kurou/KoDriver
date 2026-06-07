package kurou.kodriver.feature.readout.vehicleapproach

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.DetailPaneTitle
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

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
            label = "縦方向: ${uiState.longitudinalThresholdMeters.roundToInt()} m",
            value = uiState.longitudinalThresholdMeters.toFloat(),
            valueRange = 1f..10f,
            onValueChangeFinished = { onLongitudinalThresholdChanged(it.toDouble()) },
        )
        ThresholdSlider(
            label = "横方向: ${"%.1f".format(uiState.lateralThresholdMeters)} m",
            value = uiState.lateralThresholdMeters.toFloat(),
            valueRange = 0.5f..5f,
            onValueChangeFinished = { onLateralThresholdChanged(it.toDouble()) },
        )
    }
}

@Composable
private fun ThresholdSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChangeFinished: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sliderValue = value
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(text = label)
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = valueRange,
            onValueChangeFinished = { onValueChangeFinished(sliderValue) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VehicleApproachDetailPanePreview() {
    VehicleApproachDetailPaneContent(uiState = VehicleApproachUiState())
}
