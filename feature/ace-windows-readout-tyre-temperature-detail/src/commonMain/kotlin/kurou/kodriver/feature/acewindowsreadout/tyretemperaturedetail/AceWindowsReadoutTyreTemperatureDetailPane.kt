package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneLabeledTextField
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.core.designsystem.rememberPendingText
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.findUnknownAceWindowsTyreTemperatureReadoutPlaceholders
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_description
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_label
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_reset
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_subtitle
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_celsius_insert
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_celsius_placeholder_hint
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_label
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_preview
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_reset
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_selected_icon
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_supporting
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_unavailable
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_unknown_placeholders
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_warning_card_title
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun AceWindowsReadoutTyreTemperatureDetailPane(modifier: Modifier = Modifier) {
    val viewModel: AceWindowsReadoutTyreTemperatureDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AceWindowsReadoutTyreTemperatureDetailPaneContent(
        uiState = uiState,
        onOverheatWarningEnabledChanged = viewModel::onOverheatWarningEnabledChanged,
        onHighThresholdChanged = viewModel::onHighThresholdChanged,
        onHighThresholdReset = viewModel::onHighThresholdReset,
        onOverheatReadoutTextChanged = viewModel::onOverheatReadoutTextChanged,
        onOverheatReadoutTextPreviewClicked = viewModel::onOverheatReadoutTextPreviewClicked,
        modifier = modifier,
    )
}

@Composable
internal fun AceWindowsReadoutTyreTemperatureDetailPaneContent(
    uiState: AceWindowsReadoutTyreTemperatureDetailUiState = AceWindowsReadoutTyreTemperatureDetailUiState(),
    onOverheatWarningEnabledChanged: (Boolean) -> Unit = {},
    onHighThresholdChanged: (Int) -> Unit = {},
    onHighThresholdReset: () -> Unit = {},
    onOverheatReadoutTextChanged: (String) -> Unit = {},
    onOverheatReadoutTextPreviewClicked: (String, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val labelTemplate = stringResource(Res.string.tyre_temperature_high_threshold_label)
    val highThresholdMin = ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN.value.toFloat()
    val highThresholdMax = ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX.value.toFloat()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneSubtitle(
            text = stringResource(Res.string.tyre_temperature_title),
            modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
        )
        DetailPaneDescription(
            text = stringResource(Res.string.tyre_temperature_description),
        )
        DetailPaneCard(
            title = stringResource(Res.string.tyre_temperature_overheat_warning_card_title),
            checked = uiState.overheatWarningEnabled,
            onCheckedChange = onOverheatWarningEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TyreTemperatureOverheatReadoutField(
                        text = uiState.overheatReadoutText,
                        available = uiState.isTextToSpeechAvailable,
                        onTextChanged = onOverheatReadoutTextChanged,
                        onPreviewClick = { onOverheatReadoutTextPreviewClicked(it, uiState.highThresholdCelsius) },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(KoDriverSpacing.small),
                    )
                    DetailPaneSubtitle(text = stringResource(Res.string.tyre_temperature_high_threshold_subtitle))
                    ThresholdSlider(
                        value = uiState.highThresholdCelsius.toFloat(),
                        valueRange = highThresholdMin..highThresholdMax,
                        steps = (highThresholdMax - highThresholdMin).toInt() - 1,
                        labelFormatter = { labelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = { onHighThresholdChanged(it.roundToInt()) },
                        defaultValue = ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value.toFloat(),
                        onResetToDefault = onHighThresholdReset,
                        resetContentDescription = stringResource(Res.string.tyre_temperature_high_threshold_reset),
                    )
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
    val textState = rememberPendingText(text, READOUT_CUSTOM_TEXT_MAX_LENGTH)
    val currentText = textState.currentText
    val changeText: (String) -> Unit = {
        textState.change(it)
        onTextChanged(it)
    }
    val unknownPlaceholders = findUnknownAceWindowsTyreTemperatureReadoutPlaceholders(currentText).joinToString("、")
    Column {
        val label = stringResource(Res.string.tyre_temperature_overheat_text_label)
        DetailPaneLabeledTextField(
            label = label,
            value = currentText,
            defaultValue = ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
            onResetToDefault = { changeText(ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT) },
            resetContentDescription = stringResource(Res.string.tyre_temperature_overheat_text_reset),
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
            modifier = Modifier.padding(bottom = KoDriverSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
        ) {
            AssistChip(
                onClick = { changeText(currentText + ACE_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER) },
                label = { Text(stringResource(Res.string.tyre_temperature_overheat_celsius_insert)) },
                enabled =
                    available &&
                        currentText.length + ACE_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER.length <=
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

@Preview(showBackground = true)
@Composable
private fun AceWindowsReadoutTyreTemperatureDetailPanePreview() {
    KoDriverTheme {
        AceWindowsReadoutTyreTemperatureDetailPaneContent(
            uiState = AceWindowsReadoutTyreTemperatureDetailUiState(isTextToSpeechAvailable = true),
        )
    }
}
