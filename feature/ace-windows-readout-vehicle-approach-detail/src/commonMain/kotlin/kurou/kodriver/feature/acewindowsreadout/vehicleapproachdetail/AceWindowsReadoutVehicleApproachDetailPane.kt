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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kurou.kodriver.core.designsystem.HelpIconButton
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_chip_label
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_description
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_help_description
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_help_icon_content_description
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_start_readout_switch_label
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_threshold_label
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
        onThresholdChanged = viewModel::onThresholdChanged,
        onResetThreshold = viewModel::onResetThreshold,
        onStartReadoutEnabledChanged = viewModel::onStartReadoutEnabledChanged,
        onPreviewClicked = viewModel::onPreviewClicked,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AceWindowsReadoutVehicleApproachDetailPaneContent(
    uiState: AceWindowsReadoutVehicleApproachDetailUiState = AceWindowsReadoutVehicleApproachDetailUiState(),
    onThresholdChanged: (Double) -> Unit = {},
    onResetThreshold: () -> Unit = {},
    onStartReadoutEnabledChanged: (Boolean) -> Unit = {},
    onPreviewClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val thresholdLabel = stringResource(Res.string.vehicle_approach_threshold_label)

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
                HelpIconButton(
                    contentDescription = stringResource(Res.string.vehicle_approach_help_icon_content_description),
                    sheetContent = { VehicleApproachHelpSheetContent() },
                )
            },
        )
        val resetToDefaultLabel = stringResource(Res.string.vehicle_approach_threshold_reset_to_default)
        ThresholdSlider(
            value = uiState.thresholdMeters.toFloat(),
            valueRange = 2f..10f,
            labelFormatter = { thresholdLabel.formatSliderLabel(it) },
            onValueChangeFinished = { onThresholdChanged(it.toDouble()) },
            modifier = Modifier.padding(horizontal = 16.dp),
            defaultValue = ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT.toFloat(),
            onResetToDefault = onResetThreshold,
            resetContentDescription = resetToDefaultLabel,
        )
        val chipLabel = stringResource(Res.string.vehicle_approach_chip_label)
        DetailPaneCard(
            title = stringResource(Res.string.vehicle_approach_start_readout_switch_label),
            checked = uiState.startReadoutEnabled,
            onCheckedChange = onStartReadoutEnabledChanged,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                DetailPaneCardChips(
                    chipLabels = listOf(chipLabel),
                    selectedChipLabels = setOf(chipLabel),
                    chipEnabled = uiState.startReadoutEnabled,
                    onChipClick = { onPreviewClicked() },
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
