package kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail

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
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.PlaceholderReadoutTextField
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.acewindows.model.findUnknownAceWindowsRemainingFuelReadoutPlaceholders
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_description
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_percent_insert
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_percent_placeholder_hint
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_text_label
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_text_preview
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_text_reset_to_default
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_text_selected_icon
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_text_supporting
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_text_unavailable
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_text_unknown_placeholders
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_threshold_description
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_threshold_label
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_threshold_reset
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_threshold_subtitle
import kurou.kodriver.feature.acewindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val THRESHOLD_MIN = 5f
private const val THRESHOLD_MAX = 90f

/**
 * AceWindowsAutoReadoutRemainingFuelDetail の画面を表示する Composable。
 */
@Composable
fun AceWindowsAutoReadoutRemainingFuelDetailPane(modifier: Modifier = Modifier) {
    val viewModel: AceWindowsAutoReadoutRemainingFuelDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) {
        onDispose { viewModel.onPreviewStopped() }
    }
    AceWindowsAutoReadoutRemainingFuelDetailPaneContent(
        uiState = uiState,
        onEnabledChanged = viewModel::onEnabledChanged,
        onReadoutTextChanged = viewModel::onReadoutTextChanged,
        onReadoutTextPreviewClicked = viewModel::onReadoutTextPreviewClicked,
        onThresholdChanged = viewModel::onThresholdChanged,
        onThresholdReset = viewModel::onThresholdReset,
        modifier = modifier,
    )
}

@Composable
internal fun AceWindowsAutoReadoutRemainingFuelDetailPaneContent(
    uiState: AceWindowsAutoReadoutRemainingFuelDetailUiState = AceWindowsAutoReadoutRemainingFuelDetailUiState(),
    onEnabledChanged: (Boolean) -> Unit = {},
    onReadoutTextChanged: (String) -> Unit = {},
    onReadoutTextPreviewClicked: (String, Int) -> Unit = { _, _ -> },
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
            text = stringResource(Res.string.remaining_fuel_description),
        )
        val thresholdLabelTemplate = stringResource(Res.string.remaining_fuel_threshold_label)
        DetailPaneCard(
            title = stringResource(Res.string.remaining_fuel_title),
            checked = uiState.enabled,
            onCheckedChange = onEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PlaceholderReadoutTextField(
                        label = stringResource(Res.string.remaining_fuel_text_label),
                        savedText = uiState.readoutText,
                        defaultText = ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
                        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                        isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                        onTextChanged = onReadoutTextChanged,
                        onPreviewClick = { onReadoutTextPreviewClicked(it, uiState.thresholdPercentage) },
                        placeholder = ACE_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER,
                        insertLabel = stringResource(Res.string.remaining_fuel_percent_insert),
                        placeholderHint = stringResource(Res.string.remaining_fuel_percent_placeholder_hint),
                        resetContentDescription = stringResource(Res.string.remaining_fuel_text_reset_to_default),
                        previewContentDescription = stringResource(Res.string.remaining_fuel_text_preview),
                        selectedContentDescription = stringResource(Res.string.remaining_fuel_text_selected_icon),
                        unavailableText = stringResource(Res.string.remaining_fuel_text_unavailable),
                        supportingText = stringResource(Res.string.remaining_fuel_text_supporting),
                        unknownPlaceholdersText = { currentText ->
                            findUnknownAceWindowsRemainingFuelReadoutPlaceholders(currentText)
                                .joinToString("、")
                                .takeIf { it.isNotEmpty() }
                                ?.let { stringResource(Res.string.remaining_fuel_text_unknown_placeholders, it) }
                        },
                    )
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.small,
                            ),
                    )
                    DetailPaneSubtitle(text = stringResource(Res.string.remaining_fuel_threshold_subtitle))
                    DetailPaneBodyText(
                        text =
                            stringResource(Res.string.remaining_fuel_threshold_description)
                                .formatSliderLabel(uiState.thresholdPercentage),
                    )
                    ThresholdSlider(
                        value = uiState.thresholdPercentage.toFloat(),
                        valueRange = THRESHOLD_MIN..THRESHOLD_MAX,
                        steps = (THRESHOLD_MAX - THRESHOLD_MIN).toInt() - 1,
                        labelFormatter = { thresholdLabelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = { onThresholdChanged(it.roundToInt()) },
                        defaultValue = ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT.toFloat(),
                        onResetToDefault = onThresholdReset,
                        resetContentDescription = stringResource(Res.string.remaining_fuel_threshold_reset),
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AceWindowsAutoReadoutRemainingFuelDetailPanePreview() {
    KoDriverTheme {
        AceWindowsAutoReadoutRemainingFuelDetailPaneContent(
            uiState = AceWindowsAutoReadoutRemainingFuelDetailUiState(isTextToSpeechAvailable = true),
        )
    }
}
