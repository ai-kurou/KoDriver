package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneCardTextField
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.HelpIconButton
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.findUnknownLmuWindowsTyreTemperatureReadoutPlaceholders
import kurou.kodriver.domain.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_carcass_card_title
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_celsius_insert
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_celsius_placeholder_hint
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_text_label
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_text_preview
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_text_reset
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_text_selected_icon
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_text_supporting
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_text_unavailable
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_text_unknown_placeholders
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_description
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_label
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_reset
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_subtitle
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_card_title
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phase_formation
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phase_garage
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phase_grid_walk
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phase_warm_up
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phases_help_description
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phases_help_icon_content_description
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phases_subtitle
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_celsius_insert
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_celsius_placeholder_hint
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_label
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_preview
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_reset
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_selected_icon
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_supporting
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_unavailable
import kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_unknown_placeholders
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
        onOverheatReadoutTextChanged = viewModel::onOverheatReadoutTextChanged,
        onOverheatReadoutTextPreviewClicked = viewModel::onOverheatReadoutTextPreviewClicked,
        onColdReadoutTextChanged = viewModel::onColdReadoutTextChanged,
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
    onOverheatReadoutTextChanged: (String) -> Unit = {},
    onOverheatReadoutTextPreviewClicked: (String, Int) -> Unit = { _, _ -> },
    onLowWarningEnabledChanged: (Boolean) -> Unit = {},
    onLowWarningPhaseToggled: (SessionPhase) -> Unit = {},
    onColdReadoutTextChanged: (String) -> Unit = {},
    onLowWarningPreviewClicked: (String) -> Unit = {},
    onVehicleClassSelected: (LmuWindowsVehicleClassData) -> Unit = {},
    onVehicleClassHighThresholdChanged: (LmuWindowsVehicleClassData, Int) -> Unit = { _, _ -> },
    onVehicleClassHighThresholdReset: (LmuWindowsVehicleClassData) -> Unit = {},
    modifier: Modifier = Modifier,
) {
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
        DetailPaneCard(
            title = stringResource(Res.string.tyre_temperature_carcass_card_title),
            checked = uiState.overheatWarningEnabled,
            onCheckedChange = onOverheatWarningEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TyreTemperatureOverheatReadoutField(
                        text = uiState.overheatReadoutText,
                        available = uiState.isTextToSpeechAvailable,
                        onTextChanged = onOverheatReadoutTextChanged,
                        onPreviewClick = { text ->
                            val celsius = uiState.selectedVehicleClassHighThresholdCelsius.value
                            onOverheatReadoutTextPreviewClicked(text, celsius)
                        },
                    )
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.small,
                            ),
                    )
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.tyre_temperature_vehicle_class_target_subtitle),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
                        verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
                        modifier =
                            Modifier.fillMaxWidth().padding(
                                horizontal = KoDriverSpacing.extraSmall,
                                vertical = KoDriverSpacing.extraSmall,
                            ),
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
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.small,
                            ),
                    )
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.tyre_temperature_high_threshold_subtitle),
                        trailingContent = {
                            HelpIconButton(
                                contentDescription = helpIconContentDescription,
                                sheetContent = { TyreTemperatureThresholdHelpSheetContent() },
                            )
                        },
                    )
                    val highThresholdMin = LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN.value.toFloat()
                    val highThresholdMax = LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX.value.toFloat()
                    ThresholdSlider(
                        value = uiState.selectedVehicleClassHighThresholdCelsius.value.toFloat(),
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
        DetailPaneCard(
            title = stringResource(Res.string.tyre_temperature_low_warning_card_title),
            checked = uiState.lowWarningEnabled,
            onCheckedChange = onLowWarningEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TyreTemperatureColdReadoutField(
                        text = uiState.coldReadoutText,
                        available = uiState.isTextToSpeechAvailable,
                        onTextChanged = onColdReadoutTextChanged,
                        onPreviewClick = onLowWarningPreviewClicked,
                    )
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.small,
                            ),
                    )
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.tyre_temperature_low_warning_phases_subtitle),
                        trailingContent = {
                            HelpIconButton(
                                contentDescription = lowWarningPhasesHelpIconContentDescription,
                                sheetContent = { TyreTemperatureLowWarningPhasesHelpSheetContent() },
                            )
                        },
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
                        verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
                        modifier =
                            Modifier.fillMaxWidth().padding(
                                horizontal = KoDriverSpacing.extraSmall,
                                vertical = KoDriverSpacing.extraSmall,
                            ),
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
private fun TyreTemperatureOverheatReadoutField(
    text: String,
    available: Boolean,
    onTextChanged: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
) {
    var currentText by remember { mutableStateOf(text) }
    // 古い保存結果で入力を巻き戻さず、正規化後の保存値が一致したら待機を解除する。
    var pendingText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(text, pendingText) {
        if (pendingText == null) {
            currentText = text
        } else if (pendingText == text) {
            pendingText = null
        }
    }
    val changeText: (String) -> Unit = {
        currentText = it
        pendingText = it.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH)
        onTextChanged(it)
    }
    val unknownPlaceholders = findUnknownLmuWindowsTyreTemperatureReadoutPlaceholders(currentText).joinToString("、")
    Column {
        val label = stringResource(Res.string.tyre_temperature_overheat_text_label)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        DetailPaneCardTextField(
            value = currentText,
            defaultValue = LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
            onResetToDefault = { changeText(LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT) },
            resetContentDescription = stringResource(Res.string.tyre_temperature_overheat_text_reset),
            placeholder = label,
            maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
            onValueChangeFinished = changeText,
            onPreviewClick = onPreviewClick,
            enabled = available,
            selected = currentText.isNotBlank(),
            supportingText =
                when {
                    !available -> {
                        stringResource(Res.string.tyre_temperature_overheat_text_unavailable)
                    }

                    unknownPlaceholders.isNotEmpty() -> {
                        stringResource(
                            Res.string.tyre_temperature_overheat_text_unknown_placeholders,
                            unknownPlaceholders,
                        )
                    }

                    currentText.isBlank() -> {
                        stringResource(Res.string.tyre_temperature_overheat_text_supporting)
                    }

                    else -> {
                        null
                    }
                },
            previewContentDescription = stringResource(Res.string.tyre_temperature_overheat_text_preview),
            selectedContentDescription = stringResource(Res.string.tyre_temperature_overheat_text_selected_icon),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
        ) {
            AssistChip(
                onClick = { changeText(currentText + LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER) },
                label = { Text(stringResource(Res.string.tyre_temperature_overheat_celsius_insert)) },
                enabled =
                    available &&
                        currentText.length + LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER.length <=
                        READOUT_CUSTOM_TEXT_MAX_LENGTH,
            )
            Text(
                text = stringResource(Res.string.tyre_temperature_overheat_celsius_placeholder_hint),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TyreTemperatureColdReadoutField(
    text: String,
    available: Boolean,
    onTextChanged: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
) {
    var currentText by remember { mutableStateOf(text) }
    // 古い保存結果で入力を巻き戻さず、正規化後の保存値が一致したら待機を解除する。
    var pendingText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(text, pendingText) {
        if (pendingText == null) {
            currentText = text
        } else if (pendingText == text) {
            pendingText = null
        }
    }
    val changeText: (String) -> Unit = {
        currentText = it
        pendingText = it.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH)
        onTextChanged(it)
    }
    val unknownPlaceholders = findUnknownLmuWindowsTyreTemperatureReadoutPlaceholders(currentText).joinToString("、")
    Column {
        val label = stringResource(Res.string.tyre_temperature_cold_text_label)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        DetailPaneCardTextField(
            value = currentText,
            defaultValue = LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT,
            onResetToDefault = { changeText(LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT) },
            resetContentDescription = stringResource(Res.string.tyre_temperature_cold_text_reset),
            placeholder = label,
            maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
            onValueChangeFinished = changeText,
            onPreviewClick = onPreviewClick,
            enabled = available,
            selected = currentText.isNotBlank(),
            supportingText =
                when {
                    !available -> {
                        stringResource(Res.string.tyre_temperature_cold_text_unavailable)
                    }

                    unknownPlaceholders.isNotEmpty() -> {
                        stringResource(Res.string.tyre_temperature_cold_text_unknown_placeholders, unknownPlaceholders)
                    }

                    currentText.isBlank() -> {
                        stringResource(Res.string.tyre_temperature_cold_text_supporting)
                    }

                    else -> {
                        null
                    }
                },
            previewContentDescription = stringResource(Res.string.tyre_temperature_cold_text_preview),
            selectedContentDescription = stringResource(Res.string.tyre_temperature_cold_text_selected_icon),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
        ) {
            AssistChip(
                onClick = { changeText(currentText + LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER) },
                label = { Text(stringResource(Res.string.tyre_temperature_cold_celsius_insert)) },
                enabled =
                    available &&
                        currentText.length + LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER.length <=
                        READOUT_CUSTOM_TEXT_MAX_LENGTH,
            )
            Text(
                text = stringResource(Res.string.tyre_temperature_cold_celsius_placeholder_hint),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun TyreTemperatureThresholdHelpSheetContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.tyre_temperature_threshold_help_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = KoDriverSpacing.large),
    )
    Spacer(modifier = Modifier.height(KoDriverSpacing.extraLarge))
}

@Composable
internal fun TyreTemperatureLowWarningPhasesHelpSheetContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.tyre_temperature_low_warning_phases_help_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = KoDriverSpacing.large),
    )
    Spacer(modifier = Modifier.height(KoDriverSpacing.extraLarge))
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutTyreTemperatureDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsReadoutTyreTemperatureDetailPaneContent(
            uiState = LmuWindowsReadoutTyreTemperatureDetailUiState(),
        )
    }
}
