package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.findUnknownLmuWindowsBrakeTemperatureReadoutPlaceholders
import kurou.kodriver.domain.model.lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_celsius_insert
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_celsius_placeholder_hint
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_description
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_text_label
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_text_preview
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_text_reset_to_default
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_text_selected_icon
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_text_supporting
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_text_unavailable
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_text_unknown_placeholders
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_description
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_help_description
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_help_icon_content_description
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_label
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_reset
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_subtitle
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_vehicle_class_target_subtitle
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_warning_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val THRESHOLD_MIN = LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN.toFloat()
private const val THRESHOLD_MAX = LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX.toFloat()

/**
 * LmuWindowsReadoutBrakeTemperatureDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutBrakeTemperatureDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutBrakeTemperatureDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutBrakeTemperatureDetailPaneContent(
        uiState = uiState,
        onEnabledChanged = viewModel::onEnabledChanged,
        onReadoutTextChanged = viewModel::onReadoutTextChanged,
        onReadoutTextPreviewClicked = viewModel::onReadoutTextPreviewClicked,
        onVehicleClassSelected = viewModel::onVehicleClassSelected,
        onThresholdChanged = viewModel::onVehicleClassHighThresholdChanged,
        onThresholdReset = viewModel::onVehicleClassHighThresholdReset,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun LmuWindowsReadoutBrakeTemperatureDetailPaneContent(
    uiState: LmuWindowsReadoutBrakeTemperatureDetailUiState = LmuWindowsReadoutBrakeTemperatureDetailUiState(),
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
            text = stringResource(Res.string.brake_temperature_description),
        )
        val thresholdLabelTemplate = stringResource(Res.string.brake_temperature_threshold_label)
        val helpIconContentDescription =
            stringResource(Res.string.brake_temperature_threshold_help_icon_content_description)
        DetailPaneCard(
            title = stringResource(Res.string.brake_temperature_warning_title),
            checked = uiState.enabled,
            onCheckedChange = onEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    BrakeTemperatureReadoutField(
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
                        text = stringResource(Res.string.brake_temperature_vehicle_class_target_subtitle),
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
                                    "${vehicleClass.name}（$celsius°C）" to vehicleClass
                                }
                        val selectedVehicleClassChipLabel =
                            uiState.vehicleClassHighThresholdCelsius[uiState.selectedVehicleClass]?.let { celsius ->
                                "${uiState.selectedVehicleClass.name}（$celsius°C）"
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
                        text = stringResource(Res.string.brake_temperature_threshold_subtitle),
                        trailingContent = {
                            HelpIconButton(
                                contentDescription = helpIconContentDescription,
                                sheetContent = { BrakeTemperatureThresholdHelpSheetContent() },
                            )
                        },
                    )
                    val selectedVehicleClassHighThresholdCelsius =
                        uiState.vehicleClassHighThresholdCelsius[uiState.selectedVehicleClass]
                            ?: lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(
                                uiState.selectedVehicleClass,
                            )
                    DetailPaneBodyText(
                        text =
                            stringResource(Res.string.brake_temperature_threshold_description)
                                .formatSliderLabel(selectedVehicleClassHighThresholdCelsius),
                    )
                    ThresholdSlider(
                        value = selectedVehicleClassHighThresholdCelsius.toFloat(),
                        valueRange = THRESHOLD_MIN..THRESHOLD_MAX,
                        steps = (THRESHOLD_MAX - THRESHOLD_MIN).toInt() - 1,
                        labelFormatter = { thresholdLabelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = {
                            onThresholdChanged(uiState.selectedVehicleClass, it.roundToInt())
                        },
                        defaultValue =
                            lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(
                                uiState.selectedVehicleClass,
                            ).toFloat(),
                        onResetToDefault = { onThresholdReset(uiState.selectedVehicleClass) },
                        resetContentDescription = stringResource(Res.string.brake_temperature_threshold_reset),
                    )
                }
            },
        )
    }
}

@Composable
internal fun BrakeTemperatureThresholdHelpSheetContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.brake_temperature_threshold_help_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = KoDriverSpacing.large),
    )
    Spacer(modifier = Modifier.height(KoDriverSpacing.extraLarge))
}

@Composable
private fun BrakeTemperatureReadoutField(
    text: String,
    available: Boolean,
    onTextChanged: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
) {
    var currentText by remember { mutableStateOf(text) }
    // 保存が非同期のため、入力中の最新の値と一致するまでは保存済みの古い値で入力欄を巻き戻さない
    var pendingText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(text, pendingText) {
        if (pendingText == null) {
            currentText = text
        } else if (pendingText == text) {
            // 保存値は trim と文字数制限で正規化されるため、入力欄は巻き戻さず待機状態だけ解除する
            pendingText = null
        }
    }
    val changeText: (String) -> Unit = {
        currentText = it
        pendingText = it.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH)
        onTextChanged(it)
    }
    ReadoutTextField(
        label = stringResource(Res.string.brake_temperature_text_label),
        text = currentText,
        defaultText = LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT,
        isTextToSpeechAvailable = available,
        onTextChanged = changeText,
        onPreviewClick = onPreviewClick,
        unknownPlaceholders =
            findUnknownLmuWindowsBrakeTemperatureReadoutPlaceholders(currentText)
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
                    currentText + LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER,
                )
            },
            label = { Text(stringResource(Res.string.brake_temperature_celsius_insert)) },
            enabled =
                available &&
                    currentText.length + LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER.length <=
                    READOUT_CUSTOM_TEXT_MAX_LENGTH,
        )
        Text(
            text = stringResource(Res.string.brake_temperature_celsius_placeholder_hint),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 過熱警告の文言入力と試聴を提供する。 */
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
        resetContentDescription = stringResource(Res.string.brake_temperature_text_reset_to_default),
        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
        onValueChangeFinished = onTextChanged,
        onPreviewClick = onPreviewClick,
        enabled = isTextToSpeechAvailable,
        selected = text.isNotBlank(),
        supportingText =
            when {
                !isTextToSpeechAvailable -> {
                    stringResource(Res.string.brake_temperature_text_unavailable)
                }

                unknownPlaceholders.isNotEmpty() -> {
                    stringResource(
                        Res.string.brake_temperature_text_unknown_placeholders,
                        unknownPlaceholders,
                    )
                }

                text.isNotBlank() -> {
                    null
                }

                else -> {
                    stringResource(Res.string.brake_temperature_text_supporting)
                }
            },
        previewContentDescription = stringResource(Res.string.brake_temperature_text_preview),
        selectedContentDescription = stringResource(Res.string.brake_temperature_text_selected_icon),
    )
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutBrakeTemperatureDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsReadoutBrakeTemperatureDetailPaneContent()
    }
}
