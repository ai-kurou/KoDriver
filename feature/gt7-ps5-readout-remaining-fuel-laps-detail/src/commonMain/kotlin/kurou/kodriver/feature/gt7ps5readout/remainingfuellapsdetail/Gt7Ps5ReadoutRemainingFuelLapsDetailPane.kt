package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

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
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.core.designsystem.rememberPendingText
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_MAX
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_MIN
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_PLACEHOLDER
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.findUnknownGt7Ps5RemainingFuelLapsReadoutPlaceholders
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.Res
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_description
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_empty_text_label
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_enabled
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_laps_insert
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_laps_placeholder_hint
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_reset_to_default
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_slider_label
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_text_label
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_text_preview
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_text_reset_to_default
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_text_selected_icon
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_text_supporting
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_text_unavailable
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_text_unknown_placeholders
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val THRESHOLD_MIN = GT7_PS5_REMAINING_FUEL_LAPS_MIN.toFloat()
private const val THRESHOLD_MAX = GT7_PS5_REMAINING_FUEL_LAPS_MAX.toFloat()

/**
 * Gt7Ps5ReadoutRemainingFuelLapsDetail の画面を表示する Composable。
 */
@Composable
fun Gt7Ps5ReadoutRemainingFuelLapsDetailPane(modifier: Modifier = Modifier) {
    val viewModel: Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
        uiState = uiState,
        onEnabledChanged = viewModel::onEnabledChanged,
        onReadoutTextChanged = viewModel::onReadoutTextChanged,
        onEmptyReadoutTextChanged = viewModel::onEmptyReadoutTextChanged,
        onEmptyReadoutTextPreviewClicked = viewModel::onEmptyReadoutTextPreviewClicked,
        onReadoutTextPreviewClicked = viewModel::onReadoutTextPreviewClicked,
        onRemainingFuelLapsChanged = viewModel::onRemainingFuelLapsChanged,
        onResetRemainingFuelLaps = viewModel::onResetRemainingFuelLaps,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
    uiState: Gt7Ps5ReadoutRemainingFuelLapsDetailUiState =
        Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(),
    onEnabledChanged: (Boolean) -> Unit = {},
    onReadoutTextChanged: (String) -> Unit = {},
    onEmptyReadoutTextChanged: (String) -> Unit = {},
    onEmptyReadoutTextPreviewClicked: (String) -> Unit = {},
    onReadoutTextPreviewClicked: (String) -> Unit = {},
    onRemainingFuelLapsChanged: (Int) -> Unit = {},
    onResetRemainingFuelLaps: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.remaining_fuel_laps_description),
        )
        val thresholdLabelTemplate = stringResource(Res.string.remaining_fuel_laps_slider_label)
        DetailPaneCard(
            title = stringResource(Res.string.remaining_fuel_laps_enabled),
            checked = uiState.enabled,
            onCheckedChange = onEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.large)) {
                        Column {
                            RemainingFuelLapsReadoutField(
                                label = stringResource(Res.string.remaining_fuel_laps_text_label),
                                defaultText = GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT,
                                withPlaceholder = true,
                                text = uiState.readoutText,
                                available = uiState.isTextToSpeechAvailable,
                                onTextChanged = onReadoutTextChanged,
                                onPreviewClick = onReadoutTextPreviewClicked,
                            )
                        }
                        RemainingFuelLapsReadoutField(
                            label = stringResource(Res.string.remaining_fuel_laps_empty_text_label),
                            defaultText = GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT,
                            withPlaceholder = false,
                            text = uiState.emptyReadoutText,
                            available = uiState.isTextToSpeechAvailable,
                            onTextChanged = onEmptyReadoutTextChanged,
                            onPreviewClick = onEmptyReadoutTextPreviewClicked,
                        )
                    }
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.small,
                            ),
                    )
                    ThresholdSlider(
                        value = uiState.remainingFuelLaps.toFloat(),
                        valueRange = THRESHOLD_MIN..THRESHOLD_MAX,
                        steps = (THRESHOLD_MAX - THRESHOLD_MIN).toInt() - 1,
                        labelFormatter = { thresholdLabelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = { onRemainingFuelLapsChanged(it.roundToInt()) },
                        defaultValue = GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT.toFloat(),
                        onResetToDefault = onResetRemainingFuelLaps,
                        resetContentDescription = stringResource(Res.string.remaining_fuel_laps_reset_to_default),
                    )
                }
            },
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun RemainingFuelLapsReadoutField(
    label: String,
    defaultText: String,
    withPlaceholder: Boolean,
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
        label = label,
        text = currentText,
        defaultText = defaultText,
        isTextToSpeechAvailable = available,
        onTextChanged = changeText,
        onPreviewClick = onPreviewClick,
        unknownPlaceholders =
            if (withPlaceholder) {
                findUnknownGt7Ps5RemainingFuelLapsReadoutPlaceholders(currentText).joinToString("、")
            } else {
                ""
            },
    )
    if (!withPlaceholder) return
    Row(
        modifier = Modifier.padding(bottom = KoDriverSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
    ) {
        AssistChip(
            onClick = {
                changeText(
                    currentText + GT7_PS5_REMAINING_FUEL_LAPS_PLACEHOLDER,
                )
            },
            label = { Text(stringResource(Res.string.remaining_fuel_laps_laps_insert)) },
            enabled =
                available &&
                    currentText.length + GT7_PS5_REMAINING_FUEL_LAPS_PLACEHOLDER.length <=
                    READOUT_CUSTOM_TEXT_MAX_LENGTH,
        )
        Text(
            text = stringResource(Res.string.remaining_fuel_laps_laps_placeholder_hint),
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
        resetContentDescription = stringResource(Res.string.remaining_fuel_laps_text_reset_to_default),
        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
        onValueChangeFinished = onTextChanged,
        onPreviewClick = onPreviewClick,
        enabled = isTextToSpeechAvailable,
        selected = text.isNotBlank(),
        supportingText =
            when {
                !isTextToSpeechAvailable -> {
                    stringResource(Res.string.remaining_fuel_laps_text_unavailable)
                }

                unknownPlaceholders.isNotEmpty() -> {
                    stringResource(
                        Res.string.remaining_fuel_laps_text_unknown_placeholders,
                        unknownPlaceholders,
                    )
                }

                text.isNotBlank() -> {
                    null
                }

                else -> {
                    stringResource(Res.string.remaining_fuel_laps_text_supporting)
                }
            },
        previewContentDescription = stringResource(Res.string.remaining_fuel_laps_text_preview),
        selectedContentDescription = stringResource(Res.string.remaining_fuel_laps_text_selected_icon),
    )
}

@Preview(showBackground = true)
@Composable
private fun Gt7Ps5ReadoutRemainingFuelLapsDetailPanePreview() {
    KoDriverTheme {
        Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent()
    }
}
