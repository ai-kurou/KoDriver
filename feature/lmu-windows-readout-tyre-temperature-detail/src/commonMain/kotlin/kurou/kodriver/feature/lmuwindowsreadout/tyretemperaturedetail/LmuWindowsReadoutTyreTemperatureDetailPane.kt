package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_carcass_card_title
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_description
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_label
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_reset
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_subtitle
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_card_title
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_chip
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phase_formation
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phase_garage
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phase_grid_walk
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phase_warm_up
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phases_help_description
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phases_help_icon_content_description
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phases_subtitle
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_warning_chip
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_threshold_help_description
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_threshold_help_icon_content_description
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_vehicle_class_target_subtitle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

/**
 * LmuWindowsReadoutTyreTemperatureDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutTyreTemperatureDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutTyreTemperatureDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutTyreTemperatureDetailPaneContent(
        uiState = uiState,
        onOverheatWarningEnabledChanged = viewModel::onOverheatWarningEnabledChanged,
        onPreviewClicked = viewModel::onPreviewClicked,
        onLowWarningEnabledChanged = viewModel::onLowWarningEnabledChanged,
        onLowWarningPhaseToggled = viewModel::onLowWarningPhaseToggled,
        onLowWarningPreviewClicked = viewModel::onLowWarningPreviewClicked,
        onVehicleClassSelected = viewModel::onVehicleClassSelected,
        onVehicleClassHighThresholdChanged = viewModel::onVehicleClassHighThresholdChanged,
        onVehicleClassHighThresholdReset = viewModel::onVehicleClassHighThresholdReset,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LmuWindowsReadoutTyreTemperatureDetailPaneContent(
    uiState: LmuWindowsReadoutTyreTemperatureDetailUiState,
    onOverheatWarningEnabledChanged: (Boolean) -> Unit = {},
    onPreviewClicked: () -> Unit = {},
    onLowWarningEnabledChanged: (Boolean) -> Unit = {},
    onLowWarningPhaseToggled: (SessionPhase) -> Unit = {},
    onLowWarningPreviewClicked: () -> Unit = {},
    onVehicleClassSelected: (LmuWindowsVehicleClassData) -> Unit = {},
    onVehicleClassHighThresholdChanged: (LmuWindowsVehicleClassData, Int) -> Unit = { _, _ -> },
    onVehicleClassHighThresholdReset: (LmuWindowsVehicleClassData) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showHelpSheet by remember { mutableStateOf(false) }
    var showLowWarningPhasesHelpSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val lowWarningPhasesSheetState = rememberModalBottomSheetState()

    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = sheetState,
        ) {
            TyreTemperatureThresholdHelpSheetContent()
        }
    }

    if (showLowWarningPhasesHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLowWarningPhasesHelpSheet = false },
            sheetState = lowWarningPhasesSheetState,
        ) {
            TyreTemperatureLowWarningPhasesHelpSheetContent()
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.tyre_temperature_description),
        )
        val helpIconContentDescription =
            stringResource(Res.string.tyre_temperature_threshold_help_icon_content_description)
        val labelTemplate = stringResource(Res.string.tyre_temperature_high_threshold_label)
        val lowWarningPhasesHelpIconContentDescription =
            stringResource(Res.string.tyre_temperature_low_warning_phases_help_icon_content_description)
        val phaseLabels =
            mapOf(
                SessionPhase.GARAGE to stringResource(Res.string.tyre_temperature_low_warning_phase_garage),
                SessionPhase.WARM_UP to stringResource(Res.string.tyre_temperature_low_warning_phase_warm_up),
                SessionPhase.GRID_WALK to stringResource(Res.string.tyre_temperature_low_warning_phase_grid_walk),
                SessionPhase.FORMATION to stringResource(Res.string.tyre_temperature_low_warning_phase_formation),
            )
        val overheatWarningChipLabel = stringResource(Res.string.tyre_temperature_overheat_warning_chip)
        DetailPaneCard(
            title = stringResource(Res.string.tyre_temperature_carcass_card_title),
            checked = uiState.overheatWarningEnabled,
            onCheckedChange = onOverheatWarningEnabledChanged,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        DetailPaneCardChips(
                            chipLabels = listOf(overheatWarningChipLabel),
                            selectedChipLabels = setOf(overheatWarningChipLabel),
                            chipEnabled = uiState.overheatWarningEnabled,
                            onChipClick = { onPreviewClicked() },
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.tyre_temperature_vehicle_class_target_subtitle),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    ) {
                        val vehicleClassByChipLabel =
                            uiState.vehicleClassHighThresholdCelsius
                                .filterKeys { it !is LmuWindowsVehicleClassData.Unknown }
                                .entries
                                .associate { (vehicleClass, celsius) ->
                                    "${vehicleClass.name}（$celsius°C）" to
                                        vehicleClass
                                }
                        val selectedVehicleClassChipLabel =
                            uiState.vehicleClassHighThresholdCelsius[uiState.selectedVehicleClass]?.let { celsius ->
                                "${uiState.selectedVehicleClass.name}（$celsius°C）"
                            }
                        DetailPaneCardChips(
                            chipLabels = vehicleClassByChipLabel.keys.toList(),
                            selectedChipLabels = setOfNotNull(selectedVehicleClassChipLabel),
                            chipEnabled = uiState.overheatWarningEnabled,
                            onChipClick = { label ->
                                vehicleClassByChipLabel[label]?.let { onVehicleClassSelected(it) }
                            },
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.tyre_temperature_high_threshold_subtitle),
                        trailingContent = {
                            IconButton(onClick = { showHelpSheet = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = helpIconContentDescription,
                                    tint = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        },
                    )
                    val selectedVehicleClassHighThresholdCelsius =
                        uiState.vehicleClassHighThresholdCelsius[uiState.selectedVehicleClass]
                            ?: lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(
                                uiState.selectedVehicleClass,
                            ).value
                    val highThresholdMin = LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN.value.toFloat()
                    val highThresholdMax = LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX.value.toFloat()
                    ThresholdSlider(
                        value = selectedVehicleClassHighThresholdCelsius.toFloat(),
                        valueRange = highThresholdMin..highThresholdMax,
                        steps = (highThresholdMax - highThresholdMin).toInt() - 1,
                        labelFormatter = { labelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = {
                            onVehicleClassHighThresholdChanged(uiState.selectedVehicleClass, it.roundToInt())
                        },
                        defaultValue =
                            lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(
                                uiState.selectedVehicleClass,
                            ).value.toFloat(),
                        onResetToDefault = { onVehicleClassHighThresholdReset(uiState.selectedVehicleClass) },
                        resetContentDescription = stringResource(Res.string.tyre_temperature_high_threshold_reset),
                    )
                }
            },
        )
        val lowWarningChipLabel = stringResource(Res.string.tyre_temperature_low_warning_chip)
        DetailPaneCard(
            title = stringResource(Res.string.tyre_temperature_low_warning_card_title),
            checked = uiState.lowWarningEnabled,
            onCheckedChange = onLowWarningEnabledChanged,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        DetailPaneCardChips(
                            chipLabels = listOf(lowWarningChipLabel),
                            selectedChipLabels = setOf(lowWarningChipLabel),
                            chipEnabled = uiState.lowWarningEnabled,
                            onChipClick = { onLowWarningPreviewClicked() },
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.tyre_temperature_low_warning_phases_subtitle),
                        trailingContent = {
                            IconButton(onClick = { showLowWarningPhasesHelpSheet = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = lowWarningPhasesHelpIconContentDescription,
                                    tint = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        },
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    ) {
                        phaseLabels.forEach { (phase, label) ->
                            val selected = phase in uiState.lowWarningPhases
                            FilterChip(
                                selected = selected,
                                onClick = { onLowWarningPhaseToggled(phase) },
                                label = { Text(text = label) },
                                leadingIcon =
                                    if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                            )
                                        }
                                    } else {
                                        null
                                    },
                            )
                        }
                    }
                }
            },
        )
    }
}

@Composable
internal fun TyreTemperatureThresholdHelpSheetContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.tyre_temperature_threshold_help_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
internal fun TyreTemperatureLowWarningPhasesHelpSheetContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.tyre_temperature_low_warning_phases_help_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutTyreTemperatureDetailPanePreview() {
    LmuWindowsReadoutTyreTemperatureDetailPaneContent(
        uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(),
    )
}
