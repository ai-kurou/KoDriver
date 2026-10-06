package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

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
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneLabeledTextField
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.findUnknownGt7Ps5TyreTemperatureReadoutPlaceholders
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.Res
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_celsius_insert
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_celsius_placeholder_hint
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_description
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_label
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_reset
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_subtitle
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_warning_card_title
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_text_label
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_text_preview
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_text_reset_to_default
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_text_selected_icon
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_text_supporting
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_text_unavailable
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_text_unknown_placeholders
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

/**
 * Gt7Ps5ReadoutTyreTemperatureDetail の画面を表示する Composable。
 */
@Composable
fun Gt7Ps5ReadoutTyreTemperatureDetailPane(modifier: Modifier = Modifier) {
    val viewModel: Gt7Ps5ReadoutTyreTemperatureDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Gt7Ps5ReadoutTyreTemperatureDetailPaneContent(
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
internal fun Gt7Ps5ReadoutTyreTemperatureDetailPaneContent(
    uiState: Gt7Ps5ReadoutTyreTemperatureDetailUiState = Gt7Ps5ReadoutTyreTemperatureDetailUiState(),
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
                    TyreTemperatureReadoutField(
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

@Composable
private fun TyreTemperatureReadoutField(
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
        label = stringResource(Res.string.tyre_temperature_text_label),
        text = currentText,
        defaultText = GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
        isTextToSpeechAvailable = available,
        onTextChanged = changeText,
        onPreviewClick = onPreviewClick,
        unknownPlaceholders =
            findUnknownGt7Ps5TyreTemperatureReadoutPlaceholders(currentText)
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
                    currentText + GT7_PS5_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER,
                )
            },
            label = { Text(stringResource(Res.string.tyre_temperature_celsius_insert)) },
            enabled =
                available &&
                    currentText.length + GT7_PS5_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER.length <=
                    READOUT_CUSTOM_TEXT_MAX_LENGTH,
        )
        Text(
            text = stringResource(Res.string.tyre_temperature_celsius_placeholder_hint),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** タイヤ過熱警告の文言入力と試聴を提供する。 */
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
        resetContentDescription = stringResource(Res.string.tyre_temperature_text_reset_to_default),
        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
        onValueChangeFinished = onTextChanged,
        onPreviewClick = onPreviewClick,
        enabled = isTextToSpeechAvailable,
        selected = text.isNotBlank(),
        supportingText =
            when {
                !isTextToSpeechAvailable -> {
                    stringResource(Res.string.tyre_temperature_text_unavailable)
                }

                unknownPlaceholders.isNotEmpty() -> {
                    stringResource(
                        Res.string.tyre_temperature_text_unknown_placeholders,
                        unknownPlaceholders,
                    )
                }

                text.isNotBlank() -> {
                    null
                }

                else -> {
                    stringResource(Res.string.tyre_temperature_text_supporting)
                }
            },
        previewContentDescription = stringResource(Res.string.tyre_temperature_text_preview),
        selectedContentDescription = stringResource(Res.string.tyre_temperature_text_selected_icon),
    )
}

@Preview(showBackground = true)
@Composable
private fun Gt7Ps5ReadoutTyreTemperatureDetailPanePreview() {
    KoDriverTheme {
        Gt7Ps5ReadoutTyreTemperatureDetailPaneContent()
    }
}
