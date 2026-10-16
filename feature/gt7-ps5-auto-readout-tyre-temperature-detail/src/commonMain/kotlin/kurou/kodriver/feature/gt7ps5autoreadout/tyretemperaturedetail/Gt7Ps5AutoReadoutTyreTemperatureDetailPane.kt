package kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.PlaceholderReadoutTextField
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.findUnknownGt7Ps5TyreTemperatureReadoutPlaceholders
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.Res
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_celsius_insert
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_celsius_placeholder_hint
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_description
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_label
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_reset
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_subtitle
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_warning_card_title
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_text_label
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_text_preview
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_text_reset_to_default
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_text_selected_icon
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_text_supporting
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_text_unavailable
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_text_unknown_placeholders
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

/**
 * Gt7Ps5AutoReadoutTyreTemperatureDetail の画面を表示する Composable。
 */
@Composable
fun Gt7Ps5AutoReadoutTyreTemperatureDetailPane(modifier: Modifier = Modifier) {
    val viewModel: Gt7Ps5AutoReadoutTyreTemperatureDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) {
        onDispose { viewModel.onPreviewStopped() }
    }
    Gt7Ps5AutoReadoutTyreTemperatureDetailPaneContent(
        uiState = uiState,
        onOverheatWarningEnabledChanged = viewModel::onOverheatWarningEnabledChanged,
        onHighThresholdChanged = viewModel::onHighThresholdChanged,
        onHighThresholdReset = viewModel::onHighThresholdReset,
        onReadoutTextChanged = viewModel::onReadoutTextChanged,
        onReadoutTextPreviewClicked = viewModel::onReadoutTextPreviewClicked,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun Gt7Ps5AutoReadoutTyreTemperatureDetailPaneContent(
    uiState: Gt7Ps5AutoReadoutTyreTemperatureDetailUiState = Gt7Ps5AutoReadoutTyreTemperatureDetailUiState(),
    onOverheatWarningEnabledChanged: (Boolean) -> Unit = {},
    onHighThresholdChanged: (Int) -> Unit = {},
    onHighThresholdReset: () -> Unit = {},
    onReadoutTextChanged: (String) -> Unit = {},
    onReadoutTextPreviewClicked: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val labelTemplate = stringResource(Res.string.tyre_temperature_high_threshold_label)
    val highThresholdMin = GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN.value.toFloat()
    val highThresholdMax = GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX.value.toFloat()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
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
                    PlaceholderReadoutTextField(
                        label = stringResource(Res.string.tyre_temperature_text_label),
                        savedText = uiState.readoutText,
                        defaultText = GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
                        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                        isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                        onTextChanged = onReadoutTextChanged,
                        onPreviewClick = onReadoutTextPreviewClicked,
                        placeholder = GT7_PS5_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER,
                        insertLabel = stringResource(Res.string.tyre_temperature_celsius_insert),
                        placeholderHint = stringResource(Res.string.tyre_temperature_celsius_placeholder_hint),
                        resetContentDescription = stringResource(Res.string.tyre_temperature_text_reset_to_default),
                        previewContentDescription = stringResource(Res.string.tyre_temperature_text_preview),
                        selectedContentDescription = stringResource(Res.string.tyre_temperature_text_selected_icon),
                        unavailableText = stringResource(Res.string.tyre_temperature_text_unavailable),
                        supportingText = stringResource(Res.string.tyre_temperature_text_supporting),
                        unknownPlaceholdersText = { currentText ->
                            findUnknownGt7Ps5TyreTemperatureReadoutPlaceholders(currentText)
                                .joinToString("、")
                                .takeIf { it.isNotEmpty() }
                                ?.let { stringResource(Res.string.tyre_temperature_text_unknown_placeholders, it) }
                        },
                    )
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.small,
                            ),
                    )
                    DetailPaneSubtitle(text = stringResource(Res.string.tyre_temperature_high_threshold_subtitle))
                    ThresholdSlider(
                        value = uiState.highThresholdCelsius.toFloat(),
                        valueRange = highThresholdMin..highThresholdMax,
                        steps = (highThresholdMax - highThresholdMin).toInt() - 1,
                        labelFormatter = { labelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = { onHighThresholdChanged(it.roundToInt()) },
                        defaultValue = GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value.toFloat(),
                        onResetToDefault = onHighThresholdReset,
                        resetContentDescription = stringResource(Res.string.tyre_temperature_high_threshold_reset),
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Gt7Ps5AutoReadoutTyreTemperatureDetailPanePreview() {
    KoDriverTheme {
        Gt7Ps5AutoReadoutTyreTemperatureDetailPaneContent()
    }
}
