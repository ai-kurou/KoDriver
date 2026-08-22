package kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_car_left_right_chip_label
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_description
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_help_description
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_help_icon_content_description
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_lateral_label
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_left_right_approach_chip_label
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_longitudinal_label
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_start_readout_switch_label
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_threshold_reset_to_default
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_threshold_subtitle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * AceWindowsReadoutVehicleApproachDetail の画面を表示する Composable。
 */
@Composable
fun AceWindowsReadoutVehicleApproachDetailPane(modifier: Modifier = Modifier) {
    val viewModel: AceWindowsReadoutVehicleApproachDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AceWindowsReadoutVehicleApproachDetailPaneContent(
        uiState = uiState,
        onLongitudinalThresholdChanged = viewModel::onLongitudinalThresholdChanged,
        onResetLongitudinalThreshold = viewModel::onResetLongitudinalThreshold,
        onLateralThresholdChanged = viewModel::onLateralThresholdChanged,
        onResetLateralThreshold = viewModel::onResetLateralThreshold,
        onStartReadoutEnabledChanged = viewModel::onStartReadoutEnabledChanged,
        onStartReadoutTypeChanged = viewModel::onStartReadoutTypeChanged,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AceWindowsReadoutVehicleApproachDetailPaneContent(
    uiState: AceWindowsReadoutVehicleApproachDetailUiState = AceWindowsReadoutVehicleApproachDetailUiState(),
    onLongitudinalThresholdChanged: (Double) -> Unit = {},
    onResetLongitudinalThreshold: () -> Unit = {},
    onLateralThresholdChanged: (Double) -> Unit = {},
    onResetLateralThreshold: () -> Unit = {},
    onStartReadoutEnabledChanged: (Boolean) -> Unit = {},
    onStartReadoutTypeChanged: (VehicleApproachStartReadoutType) -> Unit = {},
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

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.vehicle_approach_description),
        )
        DetailPaneSubtitle(
            text = stringResource(Res.string.vehicle_approach_threshold_subtitle),
            modifier = Modifier.padding(horizontal = 16.dp),
            trailingContent = {
                IconButton(onClick = { showHelpSheet = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = stringResource(Res.string.vehicle_approach_help_icon_content_description),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            },
        )
        val resetToDefaultLabel = stringResource(Res.string.vehicle_approach_threshold_reset_to_default)
        val defaultLongitudinalThresholdMeters =
            ACE_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
        val defaultLateralThresholdMeters = ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
        ThresholdSlider(
            value = uiState.longitudinalThresholdMeters.toFloat(),
            valueRange = 0.1f..10f,
            labelFormatter = { longitudinalLabel.formatSliderLabel(it) },
            onValueChangeFinished = { onLongitudinalThresholdChanged(it.toDouble()) },
            modifier = Modifier.padding(horizontal = 16.dp),
            defaultValue = defaultLongitudinalThresholdMeters.toFloat(),
            onResetToDefault = onResetLongitudinalThreshold,
            resetContentDescription = resetToDefaultLabel,
        )
        ThresholdSlider(
            value = uiState.lateralThresholdMeters.toFloat(),
            valueRange = 2f..8f,
            labelFormatter = { lateralLabel.formatSliderLabel(it) },
            onValueChangeFinished = { onLateralThresholdChanged(it.toDouble()) },
            modifier = Modifier.padding(horizontal = 16.dp),
            defaultValue = defaultLateralThresholdMeters.toFloat(),
            onResetToDefault = onResetLateralThreshold,
            resetContentDescription = resetToDefaultLabel,
        )
        val carLeftRightChipLabel = stringResource(Res.string.vehicle_approach_car_left_right_chip_label)
        val leftRightApproachChipLabel = stringResource(Res.string.vehicle_approach_left_right_approach_chip_label)
        val startReadoutTypeLabels =
            mapOf(
                VehicleApproachStartReadoutType.CAR_LEFT_RIGHT to carLeftRightChipLabel,
                VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH to leftRightApproachChipLabel,
            )
        DetailPaneCard(
            title = stringResource(Res.string.vehicle_approach_start_readout_switch_label),
            checked = uiState.startReadoutEnabled,
            onCheckedChange = onStartReadoutEnabledChanged,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                DetailPaneCardChips(
                    chipLabels = startReadoutTypeLabels.values.toList(),
                    selectedChipLabels = setOfNotNull(startReadoutTypeLabels[uiState.startReadoutType]),
                    chipEnabled = uiState.startReadoutEnabled,
                    onChipClick = { label ->
                        startReadoutTypeLabels
                            .entries
                            .firstOrNull { it.value == label }
                            ?.let { onStartReadoutTypeChanged(it.key) }
                    },
                )
            },
        )
    }
}

@Composable
internal fun VehicleApproachHelpSheetContent(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
private fun AceWindowsReadoutVehicleApproachDetailPanePreview() {
    AceWindowsReadoutVehicleApproachDetailPaneContent()
}
