package kurou.kodriver.feature.readout.vehicleapproach

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.DetailPaneTitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import kodriver.feature.readout.vehicleapproach.generated.resources.Res
import kodriver.feature.readout.vehicleapproach.generated.resources.vehicle_approach
import kodriver.feature.readout.vehicleapproach.generated.resources.vehicle_approach_description
import kodriver.feature.readout.vehicleapproach.generated.resources.vehicle_approach_help_description
import kodriver.feature.readout.vehicleapproach.generated.resources.vehicle_approach_lateral_label
import kodriver.feature.readout.vehicleapproach.generated.resources.vehicle_approach_longitudinal_label
import kodriver.feature.readout.vehicleapproach.generated.resources.vehicle_approach_threshold_subtitle
import kodriver.feature.readout.vehicleapproach.generated.resources.vehicle_approach_title
import org.jetbrains.compose.resources.painterResource
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VehicleApproachDetailPaneContent(
    uiState: VehicleApproachUiState,
    onLongitudinalThresholdChanged: (Double) -> Unit = {},
    onLateralThresholdChanged: (Double) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val longitudinalLabel = stringResource(Res.string.vehicle_approach_longitudinal_label)
    val lateralLabel = stringResource(Res.string.vehicle_approach_lateral_label)
    var showHelpSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = sheetState,
        ) {
            Image(
                painter = painterResource(Res.drawable.vehicle_approach),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(0.7f).align(Alignment.CenterHorizontally).padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.vehicle_approach_help_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        DetailPaneTitle(title = stringResource(Res.string.vehicle_approach_title))
        DetailPaneDescription(text = stringResource(Res.string.vehicle_approach_description))
        DetailPaneSubtitle(
            text = stringResource(Res.string.vehicle_approach_threshold_subtitle),
            trailingContent = {
                IconButton(
                    onClick = { showHelpSheet = true },
                    modifier = Modifier.testTag("vehicle_approach_help_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            },
        )
        ThresholdSlider(
            value = uiState.longitudinalThresholdMeters.toFloat(),
            valueRange = 0.1f..5f,
            labelFormatter = { longitudinalLabel.format(it) },
            onValueChangeFinished = { onLongitudinalThresholdChanged(it.toDouble()) },
        )
        ThresholdSlider(
            value = uiState.lateralThresholdMeters.toFloat(),
            valueRange = 2f..8f,
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
