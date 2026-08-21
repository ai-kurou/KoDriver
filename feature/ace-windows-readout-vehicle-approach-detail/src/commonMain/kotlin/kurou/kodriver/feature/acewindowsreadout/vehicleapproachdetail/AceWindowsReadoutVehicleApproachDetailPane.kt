package kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_ENABLED_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_description
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_start_readout_card_title
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_threshold_label
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_threshold_reset_to_default
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_threshold_subtitle
import org.jetbrains.compose.resources.stringResource

private const val THRESHOLD_MIN_METERS = 5f
private const val THRESHOLD_MAX_METERS = 20f

/**
 * AceWindowsReadoutVehicleApproachDetail の画面を表示する Composable。
 *
 * 設定値の永続化は未実装で、画面内でのみ保持する（別PRで DataStore への永続化を行う）。
 */
@Composable
fun AceWindowsReadoutVehicleApproachDetailPane(modifier: Modifier = Modifier) {
    var startReadoutEnabled by remember {
        mutableStateOf(ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_ENABLED_DEFAULT)
    }
    var thresholdMeters by remember {
        mutableDoubleStateOf(ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT)
    }
    AceWindowsReadoutVehicleApproachDetailPaneContent(
        startReadoutEnabled = startReadoutEnabled,
        onStartReadoutEnabledChanged = { startReadoutEnabled = it },
        thresholdMeters = thresholdMeters,
        onThresholdChanged = { thresholdMeters = it },
        onResetThreshold = { thresholdMeters = ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT },
        modifier = modifier,
    )
}

@Composable
internal fun AceWindowsReadoutVehicleApproachDetailPaneContent(
    startReadoutEnabled: Boolean = ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_ENABLED_DEFAULT,
    onStartReadoutEnabledChanged: (Boolean) -> Unit = {},
    thresholdMeters: Double = ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT,
    onThresholdChanged: (Double) -> Unit = {},
    onResetThreshold: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val thresholdLabelTemplate = stringResource(Res.string.vehicle_approach_threshold_label)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.vehicle_approach_description),
        )
        DetailPaneCard(
            title = stringResource(Res.string.vehicle_approach_start_readout_card_title),
            checked = startReadoutEnabled,
            onCheckedChange = onStartReadoutEnabledChanged,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DetailPaneSubtitle(text = stringResource(Res.string.vehicle_approach_threshold_subtitle))
                    ThresholdSlider(
                        value = thresholdMeters.toFloat(),
                        valueRange = THRESHOLD_MIN_METERS..THRESHOLD_MAX_METERS,
                        labelFormatter = { thresholdLabelTemplate.formatSliderLabel(it) },
                        onValueChangeFinished = { onThresholdChanged(it.toDouble()) },
                        defaultValue = ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT.toFloat(),
                        onResetToDefault = onResetThreshold,
                        resetContentDescription =
                            stringResource(Res.string.vehicle_approach_threshold_reset_to_default),
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AceWindowsReadoutVehicleApproachDetailPanePreview() {
    AceWindowsReadoutVehicleApproachDetailPaneContent()
}
