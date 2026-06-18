package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
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
import kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.Res
import kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach
import kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_description
import kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_first_lap_subtitle
import kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_help_description
import kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_lateral_label
import kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_longitudinal_label
import kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_readout_subtitle
import kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_skip_first_lap_subtitle
import kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_threshold_subtitle
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LmuWindowsReadoutVehicleApproachDetailPane(
    modifier: Modifier = Modifier,
) {
    LmuWindowsReadoutVehicleApproachDetailPane(
        modifier = modifier,
        viewModel = koinViewModel(),
    )
}

@Composable
private fun LmuWindowsReadoutVehicleApproachDetailPane(
    modifier: Modifier = Modifier,
    viewModel: LmuWindowsReadoutVehicleApproachDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutVehicleApproachDetailPaneContent(
        uiState = uiState,
        onLongitudinalThresholdChanged = viewModel::onLongitudinalThresholdChanged,
        onLateralThresholdChanged = viewModel::onLateralThresholdChanged,
        onSkipFirstLapChanged = viewModel::onSkipFirstLapChanged,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LmuWindowsReadoutVehicleApproachDetailPaneContent(
    uiState: LmuWindowsReadoutVehicleApproachDetailUiState,
    onLongitudinalThresholdChanged: (Double) -> Unit = {},
    onLateralThresholdChanged: (Double) -> Unit = {},
    onSkipFirstLapChanged: (Boolean) -> Unit = {},
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
            VehicleApproachHelpSheetContent()
        }
    }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
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
            valueRange = 0.1f..10f,
            labelFormatter = { longitudinalLabel.format(it) },
            onValueChangeFinished = { onLongitudinalThresholdChanged(it.toDouble()) },
        )
        ThresholdSlider(
            value = uiState.lateralThresholdMeters.toFloat(),
            valueRange = 2f..8f,
            labelFormatter = { lateralLabel.format(it) },
            onValueChangeFinished = { onLateralThresholdChanged(it.toDouble()) },
        )
        DetailPaneSubtitle(text = stringResource(Res.string.vehicle_approach_first_lap_subtitle))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            Text(
                text = stringResource(Res.string.vehicle_approach_skip_first_lap_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = uiState.skipFirstLap,
                onCheckedChange = onSkipFirstLapChanged,
                modifier = Modifier.testTag("vehicle_approach_skip_first_lap_switch"),
            )
        }
        DetailPaneSubtitle(text = stringResource(Res.string.vehicle_approach_readout_subtitle))
    }
}

@Composable
internal fun VehicleApproachHelpSheetContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Text(
            text = stringResource(Res.string.vehicle_approach_help_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Image(
            painter = painterResource(Res.drawable.vehicle_approach),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(0.3f).padding(start = 16.dp),
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutVehicleApproachDetailPanePreview() {
    LmuWindowsReadoutVehicleApproachDetailPaneContent(uiState = LmuWindowsReadoutVehicleApproachDetailUiState())
}
