package kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail

import androidx.compose.foundation.layout.Arrangement
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
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.PlaceholderReadoutTextField
import kurou.kodriver.core.designsystem.ReadoutTextField
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.core.designsystem.rememberPendingText
import kurou.kodriver.core.designsystem.rememberPendingThreshold
import kurou.kodriver.core.designsystem.rememberTextInsertState
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_MAX
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_MIN
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_PLACEHOLDER
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.findUnknownAceWindowsRemainingFuelLapsReadoutPlaceholders
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_description
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_empty_text_label
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_enabled
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_laps_insert
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_laps_placeholder_hint
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_reset_to_default
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_slider_label
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_text_label
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_text_preview
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_text_reset_to_default
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_text_selected_icon
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_text_supporting
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_text_unavailable
import kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_text_unknown_placeholders
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val THRESHOLD_MIN = ACE_WINDOWS_REMAINING_FUEL_LAPS_MIN.toFloat()
private const val THRESHOLD_MAX = ACE_WINDOWS_REMAINING_FUEL_LAPS_MAX.toFloat()

/**
 * AceWindowsAutoReadoutRemainingFuelLapsDetail の画面を表示する Composable。
 */
@Composable
fun AceWindowsAutoReadoutRemainingFuelLapsDetailPane(modifier: Modifier = Modifier) {
    val viewModel: AceWindowsAutoReadoutRemainingFuelLapsDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) {
        onDispose { viewModel.onPreviewStopped() }
    }
    AceWindowsAutoReadoutRemainingFuelLapsDetailPaneContent(
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
internal fun AceWindowsAutoReadoutRemainingFuelLapsDetailPaneContent(
    uiState: AceWindowsAutoReadoutRemainingFuelLapsDetailUiState =
        AceWindowsAutoReadoutRemainingFuelLapsDetailUiState(),
    onEnabledChanged: (Boolean) -> Unit = {},
    onReadoutTextChanged: (String) -> Unit = {},
    onEmptyReadoutTextChanged: (String) -> Unit = {},
    onEmptyReadoutTextPreviewClicked: (String) -> Unit = {},
    onReadoutTextPreviewClicked: (String, Int) -> Unit = { _, _ -> },
    onRemainingFuelLapsChanged: (Int) -> Unit = {},
    onResetRemainingFuelLaps: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val thresholdState = rememberPendingThreshold(uiState.remainingFuelLaps)
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
                            PlaceholderReadoutTextField(
                                label = stringResource(Res.string.remaining_fuel_laps_text_label),
                                savedText = uiState.readoutText,
                                defaultText = ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT,
                                maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                                isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                                onTextChanged = onReadoutTextChanged,
                                onPreviewClick = { onReadoutTextPreviewClicked(it, thresholdState.currentValue) },
                                placeholder = ACE_WINDOWS_REMAINING_FUEL_LAPS_PLACEHOLDER,
                                insertLabel = stringResource(Res.string.remaining_fuel_laps_laps_insert),
                                placeholderHint = stringResource(Res.string.remaining_fuel_laps_laps_placeholder_hint),
                                resetContentDescription =
                                    stringResource(Res.string.remaining_fuel_laps_text_reset_to_default),
                                previewContentDescription = stringResource(Res.string.remaining_fuel_laps_text_preview),
                                selectedContentDescription =
                                    stringResource(Res.string.remaining_fuel_laps_text_selected_icon),
                                unavailableText = stringResource(Res.string.remaining_fuel_laps_text_unavailable),
                                supportingText = stringResource(Res.string.remaining_fuel_laps_text_supporting),
                                unknownPlaceholdersText = { currentText ->
                                    findUnknownAceWindowsRemainingFuelLapsReadoutPlaceholders(currentText)
                                        .joinToString("、")
                                        .takeIf { it.isNotEmpty() }
                                        ?.let {
                                            stringResource(Res.string.remaining_fuel_laps_text_unknown_placeholders, it)
                                        }
                                },
                            )
                        }
                        EmptyReadoutTextField(
                            label = stringResource(Res.string.remaining_fuel_laps_empty_text_label),
                            defaultText = ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT,
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
                        value = thresholdState.currentValue.toFloat(),
                        valueRange = THRESHOLD_MIN..THRESHOLD_MAX,
                        steps = (THRESHOLD_MAX - THRESHOLD_MIN).toInt() - 1,
                        labelFormatter = { thresholdLabelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = {
                            val laps = it.roundToInt()
                            thresholdState.finishChange(laps)
                            onRemainingFuelLapsChanged(laps)
                        },
                        defaultValue = ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT.toFloat(),
                        onResetToDefault = {
                            thresholdState.finishChange(ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT)
                            onResetRemainingFuelLaps()
                        },
                        onValueChange = { thresholdState.change(it.roundToInt()) },
                        resetContentDescription = stringResource(Res.string.remaining_fuel_laps_reset_to_default),
                    )
                }
            },
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun EmptyReadoutTextField(
    label: String,
    defaultText: String,
    text: String,
    available: Boolean,
    onTextChanged: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
) {
    val textState = rememberPendingText(text, READOUT_CUSTOM_TEXT_MAX_LENGTH)
    val changeText: (String) -> Unit = {
        textState.change(it)
        onTextChanged(it)
    }
    ReadoutTextField(
        label = label,
        text = textState.currentText,
        defaultText = defaultText,
        isTextToSpeechAvailable = available,
        onTextChanged = changeText,
        onPreviewClick = onPreviewClick,
        insertState = rememberTextInsertState(),
        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
        resetContentDescription = stringResource(Res.string.remaining_fuel_laps_text_reset_to_default),
        previewContentDescription = stringResource(Res.string.remaining_fuel_laps_text_preview),
        selectedContentDescription = stringResource(Res.string.remaining_fuel_laps_text_selected_icon),
        unavailableText = stringResource(Res.string.remaining_fuel_laps_text_unavailable),
        supportingText = stringResource(Res.string.remaining_fuel_laps_text_supporting),
    )
}

@Preview(showBackground = true)
@Composable
private fun AceWindowsAutoReadoutRemainingFuelLapsDetailPanePreview() {
    KoDriverTheme {
        AceWindowsAutoReadoutRemainingFuelLapsDetailPaneContent()
    }
}
