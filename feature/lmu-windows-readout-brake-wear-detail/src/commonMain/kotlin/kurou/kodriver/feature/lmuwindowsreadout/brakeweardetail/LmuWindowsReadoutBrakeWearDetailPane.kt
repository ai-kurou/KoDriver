package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneLabeledTextField
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.HelpIconButton
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.core.designsystem.rememberPendingText
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_MAX
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_MIN
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_WEAR_PERCENT_PLACEHOLDER
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearWheelRemaining
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.domain.model.findUnknownLmuWindowsBrakeWearReadoutPlaceholders
import kurou.kodriver.domain.model.lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_description
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_percent_insert
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_percent_placeholder_hint
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_remaining_title
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_text_label
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_text_preview
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_text_reset_to_default
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_text_selected_icon
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_text_supporting
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_text_unavailable
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_text_unknown_placeholders
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_threshold_description
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_threshold_help_description
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_threshold_help_icon_content_description
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_threshold_label
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_threshold_reset
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_threshold_subtitle
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_unavailable
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_vehicle_class_target_subtitle
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_warning_title
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_front_left
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_front_right
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_rear_left
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_rear_right
import kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail.generated.resources.brake_wear_wheel_row
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val THRESHOLD_MIN = LMU_WINDOWS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_MIN.toFloat()
private const val THRESHOLD_MAX = LMU_WINDOWS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_MAX.toFloat()

/**
 * LmuWindowsReadoutBrakeWearDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutBrakeWearDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutBrakeWearDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutBrakeWearDetailPaneContent(
        uiState = uiState,
        onEnabledChanged = viewModel::onEnabledChanged,
        onReadoutTextChanged = viewModel::onReadoutTextChanged,
        onReadoutTextPreviewClicked = viewModel::onReadoutTextPreviewClicked,
        onVehicleClassSelected = viewModel::onVehicleClassSelected,
        onThresholdChanged = viewModel::onVehicleClassLowThresholdChanged,
        onThresholdReset = viewModel::onVehicleClassLowThresholdReset,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun LmuWindowsReadoutBrakeWearDetailPaneContent(
    uiState: LmuWindowsReadoutBrakeWearDetailUiState = LmuWindowsReadoutBrakeWearDetailUiState(),
    onEnabledChanged: (Boolean) -> Unit = {},
    onReadoutTextChanged: (String) -> Unit = {},
    onReadoutTextPreviewClicked: (String) -> Unit = {},
    onVehicleClassSelected: (LmuWindowsVehicleClassData) -> Unit = {},
    onThresholdChanged: (LmuWindowsVehicleClassData, Int) -> Unit = { _, _ -> },
    onThresholdReset: (LmuWindowsVehicleClassData) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.brake_wear_description),
        )
        BrakeWearRemainingCard(remaining = uiState.remaining)
        val thresholdLabelTemplate = stringResource(Res.string.brake_wear_threshold_label)
        val helpIconContentDescription =
            stringResource(Res.string.brake_wear_threshold_help_icon_content_description)
        DetailPaneCard(
            title = stringResource(Res.string.brake_wear_warning_title),
            checked = uiState.enabled,
            onCheckedChange = onEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    BrakeWearReadoutField(
                        text = uiState.readoutText,
                        available = uiState.isTextToSpeechAvailable,
                        onTextChanged = onReadoutTextChanged,
                        onPreviewClick = onReadoutTextPreviewClicked,
                    )
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.small,
                            ),
                    )
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.brake_wear_vehicle_class_target_subtitle),
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
                            uiState.vehicleClassLowThresholdPercent
                                .filterKeys { it !is LmuWindowsVehicleClassData.Unknown }
                                .entries
                                .associate { (vehicleClass, percent) ->
                                    "${vehicleClass.name}（$percent%）" to vehicleClass
                                }
                        val selectedVehicleClassChipLabel =
                            uiState.vehicleClassLowThresholdPercent[uiState.selectedVehicleClass]?.let { percent ->
                                "${uiState.selectedVehicleClass.name}（$percent%）"
                            }
                        DetailPaneCardChips(
                            chipLabels = vehicleClassByChipLabel.keys.toList(),
                            selectedChipLabels = setOfNotNull(selectedVehicleClassChipLabel),
                            chipEnabled = uiState.enabled,
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
                        text = stringResource(Res.string.brake_wear_threshold_subtitle),
                        trailingContent = {
                            HelpIconButton(
                                contentDescription = helpIconContentDescription,
                                sheetContent = { BrakeWearThresholdHelpSheetContent() },
                            )
                        },
                    )
                    val selectedVehicleClassLowThresholdPercent =
                        uiState.vehicleClassLowThresholdPercent[uiState.selectedVehicleClass]
                            ?: lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(
                                uiState.selectedVehicleClass,
                            )
                    DetailPaneBodyText(
                        text =
                            stringResource(Res.string.brake_wear_threshold_description)
                                .formatSliderLabel(selectedVehicleClassLowThresholdPercent),
                    )
                    ThresholdSlider(
                        value = selectedVehicleClassLowThresholdPercent.toFloat(),
                        valueRange = THRESHOLD_MIN..THRESHOLD_MAX,
                        steps = (THRESHOLD_MAX - THRESHOLD_MIN).toInt() - 1,
                        labelFormatter = { thresholdLabelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = {
                            onThresholdChanged(uiState.selectedVehicleClass, it.roundToInt())
                        },
                        defaultValue =
                            lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(
                                uiState.selectedVehicleClass,
                            ).toFloat(),
                        onResetToDefault = { onThresholdReset(uiState.selectedVehicleClass) },
                        resetContentDescription = stringResource(Res.string.brake_wear_threshold_reset),
                    )
                }
            },
        )
    }
}

@Composable
private fun BrakeWearRemainingCard(remaining: LmuWindowsBrakeWearRemainingData?) {
    DetailPaneCard(
        title = stringResource(Res.string.brake_wear_remaining_title),
        modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
        bottomContent = {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = KoDriverSpacing.small)) {
                if (remaining == null) {
                    DetailPaneBodyText(text = stringResource(Res.string.brake_wear_unavailable))
                } else {
                    WheelIndex.entries.forEach { wheel ->
                        remaining.wheels[wheel]?.let { WheelRemainingRow(wheel, it) }
                    }
                }
            }
        },
    )
}

@Composable
private fun WheelRemainingRow(
    wheel: WheelIndex,
    remaining: LmuWindowsBrakeWearWheelRemaining,
) {
    Text(
        text =
            stringResource(
                Res.string.brake_wear_wheel_row,
                wheelLabel(wheel),
                formatBrakeWearPercent(remaining.remainingPercent),
                formatBrakeThicknessMillimeters(remaining.thickness),
            ),
        style = koDriverNumericTextStyle(MaterialTheme.typography.bodyMedium),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = KoDriverSpacing.large, vertical = KoDriverSpacing.extraSmall),
    )
}

@Composable
private fun wheelLabel(wheel: WheelIndex): String =
    when (wheel) {
        WheelIndex.FRONT_LEFT -> stringResource(Res.string.brake_wear_wheel_front_left)
        WheelIndex.FRONT_RIGHT -> stringResource(Res.string.brake_wear_wheel_front_right)
        WheelIndex.REAR_LEFT -> stringResource(Res.string.brake_wear_wheel_rear_left)
        WheelIndex.REAR_RIGHT -> stringResource(Res.string.brake_wear_wheel_rear_right)
    }

@Composable
internal fun BrakeWearThresholdHelpSheetContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.brake_wear_threshold_help_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = KoDriverSpacing.large),
    )
    Spacer(modifier = Modifier.height(KoDriverSpacing.extraLarge))
}

@Composable
private fun BrakeWearReadoutField(
    text: String,
    available: Boolean,
    onTextChanged: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
) {
    val textState = rememberPendingText(text, READOUT_CUSTOM_TEXT_MAX_LENGTH)
    val currentText = textState.currentText
    val changeText: (String) -> Unit = {
        textState.change(it)
        onTextChanged(it)
    }
    ReadoutTextField(
        label = stringResource(Res.string.brake_wear_text_label),
        text = currentText,
        defaultText = LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT,
        isTextToSpeechAvailable = available,
        onTextChanged = changeText,
        onPreviewClick = onPreviewClick,
        unknownPlaceholders =
            findUnknownLmuWindowsBrakeWearReadoutPlaceholders(currentText)
                .joinToString("、"),
    )
    Row(
        modifier = Modifier.padding(bottom = KoDriverSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
    ) {
        AssistChip(
            onClick = {
                changeText(
                    currentText + LMU_WINDOWS_BRAKE_WEAR_PERCENT_PLACEHOLDER,
                )
            },
            label = { Text(stringResource(Res.string.brake_wear_percent_insert)) },
            enabled =
                available &&
                    currentText.length + LMU_WINDOWS_BRAKE_WEAR_PERCENT_PLACEHOLDER.length <=
                    READOUT_CUSTOM_TEXT_MAX_LENGTH,
        )
        Text(
            text = stringResource(Res.string.brake_wear_percent_placeholder_hint),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 残量警告の文言入力と試聴を提供する。 */
@Suppress("LongParameterList")
@Composable
private fun ReadoutTextField(
    label: String,
    text: String,
    defaultText: String,
    isTextToSpeechAvailable: Boolean,
    onTextChanged: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
    unknownPlaceholders: String = "",
) {
    DetailPaneLabeledTextField(
        label = label,
        value = text,
        defaultValue = defaultText,
        onResetToDefault = { onTextChanged(defaultText) },
        resetContentDescription = stringResource(Res.string.brake_wear_text_reset_to_default),
        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
        onValueChangeFinished = onTextChanged,
        onPreviewClick = onPreviewClick,
        enabled = isTextToSpeechAvailable,
        selected = text.isNotBlank(),
        supportingText =
            when {
                !isTextToSpeechAvailable -> {
                    stringResource(Res.string.brake_wear_text_unavailable)
                }

                unknownPlaceholders.isNotEmpty() -> {
                    stringResource(
                        Res.string.brake_wear_text_unknown_placeholders,
                        unknownPlaceholders,
                    )
                }

                text.isNotBlank() -> {
                    null
                }

                else -> {
                    stringResource(Res.string.brake_wear_text_supporting)
                }
            },
        previewContentDescription = stringResource(Res.string.brake_wear_text_preview),
        selectedContentDescription = stringResource(Res.string.brake_wear_text_selected_icon),
    )
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutBrakeWearDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsReadoutBrakeWearDetailPaneContent()
    }
}
