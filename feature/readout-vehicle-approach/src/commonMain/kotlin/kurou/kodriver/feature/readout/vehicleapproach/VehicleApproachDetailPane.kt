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
import kodriver.feature.readout.vehicleapproach.generated.resources.Res
import kodriver.feature.readout.vehicleapproach.generated.resources.vehicle_approach_description
import kodriver.feature.readout.vehicleapproach.generated.resources.vehicle_approach_lateral_label
import kodriver.feature.readout.vehicleapproach.generated.resources.vehicle_approach_longitudinal_label
import kodriver.feature.readout.vehicleapproach.generated.resources.vehicle_approach_threshold_subtitle
import kodriver.feature.readout.vehicleapproach.generated.resources.vehicle_approach_title
import org.jetbrains.compose.resources.stringResource
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
    val longitudinalLabel = stringResource(Res.string.vehicle_approach_longitudinal_label)
    val lateralLabel = stringResource(Res.string.vehicle_approach_lateral_label)
    Column(modifier = modifier.fillMaxSize()) {
        DetailPaneTitle(title = stringResource(Res.string.vehicle_approach_title))
        DetailPaneDescription(text = stringResource(Res.string.vehicle_approach_description))
        DetailPaneSubtitle(text = stringResource(Res.string.vehicle_approach_threshold_subtitle))
        ThresholdSlider(
            value = uiState.longitudinalThresholdMeters.toFloat(),
            valueRange = 0.1f..2f,
            labelFormatter = { longitudinalLabel.format(it) },
            onValueChangeFinished = { onLongitudinalThresholdChanged(it.toDouble()) },
        )
        ThresholdSlider(
            value = uiState.lateralThresholdMeters.toFloat(),
            valueRange = 0.5f..5f,
            labelFormatter = { lateralLabel.format(it) },
            onValueChangeFinished = { onLateralThresholdChanged(it.toDouble()) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VehicleApproachDetailPanePreview() {
    VehicleApproachDetailPaneContent(uiState = VehicleApproachUiState())
}
