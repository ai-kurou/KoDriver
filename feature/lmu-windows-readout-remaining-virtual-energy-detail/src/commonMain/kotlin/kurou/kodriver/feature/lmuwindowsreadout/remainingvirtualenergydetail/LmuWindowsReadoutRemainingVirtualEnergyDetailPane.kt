package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

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
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneLabeledTextField
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.core.designsystem.rememberPendingText
import kurou.kodriver.domain.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER
import kurou.kodriver.domain.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.findUnknownLmuWindowsRemainingVirtualEnergyReadoutPlaceholders
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_description
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_percent_insert
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_percent_placeholder_hint
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_text_label
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_text_preview
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_text_reset_to_default
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_text_selected_icon
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_text_supporting
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_text_unavailable
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_text_unknown_placeholders
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_threshold_description
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_threshold_label
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_threshold_reset
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_threshold_subtitle
import kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail.generated.resources.remaining_virtual_energy_warning_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val THRESHOLD_MIN = 10f
private const val THRESHOLD_MAX = 90f

/**
 * LmuWindowsReadoutRemainingVirtualEnergyDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutRemainingVirtualEnergyDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
        uiState = uiState,
        onEnabledChanged = viewModel::onEnabledChanged,
        onReadoutTextChanged = viewModel::onReadoutTextChanged,
        onReadoutTextPreviewClicked = viewModel::onReadoutTextPreviewClicked,
        onThresholdChanged = viewModel::onThresholdChanged,
        onThresholdReset = viewModel::onThresholdReset,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
    uiState: LmuWindowsReadoutRemainingVirtualEnergyDetailUiState =
        LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(),
    onEnabledChanged: (Boolean) -> Unit = {},
    onReadoutTextChanged: (String) -> Unit = {},
    onReadoutTextPreviewClicked: (String) -> Unit = {},
    onThresholdChanged: (Int) -> Unit = {},
    onThresholdReset: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.remaining_virtual_energy_description),
        )
        val thresholdLabelTemplate = stringResource(Res.string.remaining_virtual_energy_threshold_label)
        DetailPaneCard(
            title = stringResource(Res.string.remaining_virtual_energy_warning_title),
            checked = uiState.enabled,
            onCheckedChange = onEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    RemainingVirtualEnergyReadoutField(
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
                    DetailPaneSubtitle(text = stringResource(Res.string.remaining_virtual_energy_threshold_subtitle))
                    DetailPaneBodyText(
                        text =
                            stringResource(Res.string.remaining_virtual_energy_threshold_description)
                                .formatSliderLabel(uiState.thresholdPercentage),
                    )
                    ThresholdSlider(
                        value = uiState.thresholdPercentage.toFloat(),
                        valueRange = THRESHOLD_MIN..THRESHOLD_MAX,
                        steps = (THRESHOLD_MAX - THRESHOLD_MIN).toInt() - 1,
                        labelFormatter = { thresholdLabelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = { onThresholdChanged(it.roundToInt()) },
                        defaultValue = LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT.toFloat(),
                        onResetToDefault = onThresholdReset,
                        resetContentDescription = stringResource(Res.string.remaining_virtual_energy_threshold_reset),
                    )
                }
            },
        )
    }
}

@Composable
private fun RemainingVirtualEnergyReadoutField(
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
        label = stringResource(Res.string.remaining_virtual_energy_text_label),
        text = currentText,
        defaultText = LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT,
        isTextToSpeechAvailable = available,
        onTextChanged = changeText,
        onPreviewClick = onPreviewClick,
        unknownPlaceholders =
            findUnknownLmuWindowsRemainingVirtualEnergyReadoutPlaceholders(currentText)
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
                    currentText + LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER,
                )
            },
            label = { Text(stringResource(Res.string.remaining_virtual_energy_percent_insert)) },
            enabled =
                available &&
                    currentText.length + LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER.length <=
                    READOUT_CUSTOM_TEXT_MAX_LENGTH,
        )
        Text(
            text = stringResource(Res.string.remaining_virtual_energy_percent_placeholder_hint),
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
        resetContentDescription = stringResource(Res.string.remaining_virtual_energy_text_reset_to_default),
        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
        onValueChangeFinished = onTextChanged,
        onPreviewClick = onPreviewClick,
        enabled = isTextToSpeechAvailable,
        selected = text.isNotBlank(),
        supportingText =
            when {
                !isTextToSpeechAvailable -> {
                    stringResource(Res.string.remaining_virtual_energy_text_unavailable)
                }

                unknownPlaceholders.isNotEmpty() -> {
                    stringResource(
                        Res.string.remaining_virtual_energy_text_unknown_placeholders,
                        unknownPlaceholders,
                    )
                }

                text.isNotBlank() -> {
                    null
                }

                else -> {
                    stringResource(Res.string.remaining_virtual_energy_text_supporting)
                }
            },
        previewContentDescription = stringResource(Res.string.remaining_virtual_energy_text_preview),
        selectedContentDescription = stringResource(Res.string.remaining_virtual_energy_text_selected_icon),
    )
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutRemainingVirtualEnergyDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent()
    }
}
