package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import kurou.kodriver.core.designsystem.HelpIconButton
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_LAPS_MAX
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_LAPS_MIN
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.findUnknownLmuWindowsPitTimingReadoutPlaceholders
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_description
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_imminent_text_label
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_laps_help_description
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_laps_insert
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_laps_placeholder_hint
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_laps_reset_to_default
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_laps_slider_label
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_text_label
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_text_preview
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_text_reset_to_default
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_text_selected_icon
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_text_supporting
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_text_unavailable
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_text_unknown_placeholders
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_tyre_wear_laps_help_icon_content_description
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_tyre_wear_laps_subtitle
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_tyre_wear_title
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_virtual_energy_laps_help_icon_content_description
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_virtual_energy_laps_subtitle
import kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_virtual_energy_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val MINIMUM_PIT_TIMING_LAPS = LMU_WINDOWS_PIT_TIMING_LAPS_MIN.toFloat()
private const val MAXIMUM_PIT_TIMING_LAPS = LMU_WINDOWS_PIT_TIMING_LAPS_MAX.toFloat()

/**
 * LmuWindowsReadoutPitTimingDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutPitTimingDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutPitTimingDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutPitTimingDetailPaneContent(
        uiState = uiState,
        onVirtualEnergyEnabledChanged = viewModel::onVirtualEnergyEnabledChanged,
        onVirtualEnergyLapsChanged = viewModel::onVirtualEnergyLapsChanged,
        onTyreWearEnabledChanged = viewModel::onTyreWearEnabledChanged,
        onTyreWearLapsChanged = viewModel::onTyreWearLapsChanged,
        onVirtualEnergyTextChanged = viewModel::onVirtualEnergyTextChanged,
        onTyreWearTextChanged = viewModel::onTyreWearTextChanged,
        onVirtualEnergyImminentTextChanged = viewModel::onVirtualEnergyImminentTextChanged,
        onTyreWearImminentTextChanged = viewModel::onTyreWearImminentTextChanged,
        onVirtualEnergyTextPreviewClicked = viewModel::onVirtualEnergyTextPreviewClicked,
        onTyreWearTextPreviewClicked = viewModel::onTyreWearTextPreviewClicked,
        onVirtualEnergyImminentTextPreviewClicked = viewModel::onVirtualEnergyImminentTextPreviewClicked,
        onTyreWearImminentTextPreviewClicked = viewModel::onTyreWearImminentTextPreviewClicked,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LmuWindowsReadoutPitTimingDetailPaneContent(
    uiState: LmuWindowsReadoutPitTimingDetailUiState = LmuWindowsReadoutPitTimingDetailUiState(),
    onVirtualEnergyEnabledChanged: (Boolean) -> Unit = {},
    onVirtualEnergyLapsChanged: (Int) -> Unit = {},
    onTyreWearEnabledChanged: (Boolean) -> Unit = {},
    onTyreWearLapsChanged: (Int) -> Unit = {},
    onVirtualEnergyTextChanged: (String) -> Unit = {},
    onTyreWearTextChanged: (String) -> Unit = {},
    onVirtualEnergyImminentTextChanged: (String) -> Unit = {},
    onTyreWearImminentTextChanged: (String) -> Unit = {},
    onVirtualEnergyTextPreviewClicked: (String) -> Unit = {},
    onTyreWearTextPreviewClicked: (String) -> Unit = {},
    onVirtualEnergyImminentTextPreviewClicked: (String) -> Unit = {},
    onTyreWearImminentTextPreviewClicked: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sliderLabel = stringResource(Res.string.pit_timing_laps_slider_label)
    val resetToDefaultLabel = stringResource(Res.string.pit_timing_laps_reset_to_default)
    val virtualEnergyHelpIconContentDescription =
        stringResource(Res.string.pit_timing_virtual_energy_laps_help_icon_content_description)
    val tyreWearHelpIconContentDescription =
        stringResource(Res.string.pit_timing_tyre_wear_laps_help_icon_content_description)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.pit_timing_description),
        )
        DetailPaneCard(
            title = stringResource(Res.string.pit_timing_virtual_energy_title),
            checked = uiState.virtualEnergyEnabled,
            onCheckedChange = onVirtualEnergyEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PitTimingReadoutFields(
                        text = uiState.virtualEnergyText,
                        defaultText = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT,
                        imminentText = uiState.virtualEnergyImminentText,
                        imminentDefaultText = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT,
                        available = uiState.isTextToSpeechAvailable,
                        onTextChanged = onVirtualEnergyTextChanged,
                        onImminentTextChanged = onVirtualEnergyImminentTextChanged,
                        onPreviewClick = onVirtualEnergyTextPreviewClicked,
                        onImminentPreviewClick = onVirtualEnergyImminentTextPreviewClicked,
                    )
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.medium,
                            ),
                    )
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.pit_timing_virtual_energy_laps_subtitle),
                        trailingContent = {
                            HelpIconButton(
                                contentDescription = virtualEnergyHelpIconContentDescription,
                                sheetContent = { PitTimingLapsHelpSheetContent() },
                            )
                        },
                    )
                    ThresholdSlider(
                        value = uiState.virtualEnergyLaps.toFloat(),
                        valueRange = MINIMUM_PIT_TIMING_LAPS..MAXIMUM_PIT_TIMING_LAPS,
                        labelFormatter = { sliderLabel.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = { onVirtualEnergyLapsChanged(it.roundToInt()) },
                        steps = 3,
                        defaultValue = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT.toFloat(),
                        onResetToDefault = {
                            onVirtualEnergyLapsChanged(LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT)
                        },
                        resetContentDescription = resetToDefaultLabel,
                    )
                }
            },
        )
        DetailPaneCard(
            title = stringResource(Res.string.pit_timing_tyre_wear_title),
            checked = uiState.tyreWearEnabled,
            onCheckedChange = onTyreWearEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PitTimingReadoutFields(
                        text = uiState.tyreWearText,
                        defaultText = LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT,
                        imminentText = uiState.tyreWearImminentText,
                        imminentDefaultText = LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT,
                        available = uiState.isTextToSpeechAvailable,
                        onTextChanged = onTyreWearTextChanged,
                        onImminentTextChanged = onTyreWearImminentTextChanged,
                        onPreviewClick = onTyreWearTextPreviewClicked,
                        onImminentPreviewClick = onTyreWearImminentTextPreviewClicked,
                    )
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.medium,
                            ),
                    )
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.pit_timing_tyre_wear_laps_subtitle),
                        trailingContent = {
                            HelpIconButton(
                                contentDescription = tyreWearHelpIconContentDescription,
                                sheetContent = { PitTimingLapsHelpSheetContent() },
                            )
                        },
                    )
                    ThresholdSlider(
                        value = uiState.tyreWearLaps.toFloat(),
                        valueRange = MINIMUM_PIT_TIMING_LAPS..MAXIMUM_PIT_TIMING_LAPS,
                        labelFormatter = { sliderLabel.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = { onTyreWearLapsChanged(it.roundToInt()) },
                        steps = 3,
                        defaultValue = LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT.toFloat(),
                        onResetToDefault = {
                            onTyreWearLapsChanged(LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT)
                        },
                        resetContentDescription = resetToDefaultLabel,
                    )
                }
            },
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun PitTimingReadoutFields(
    text: String,
    defaultText: String,
    imminentText: String,
    imminentDefaultText: String,
    available: Boolean,
    onTextChanged: (String) -> Unit,
    onImminentTextChanged: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
    onImminentPreviewClick: (String) -> Unit,
) {
    var currentText by remember(text) { mutableStateOf(text) }
    val changeText: (String) -> Unit = {
        currentText = it
        onTextChanged(it)
    }
    Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.large)) {
        Column {
            ReadoutTextField(
                label = stringResource(Res.string.pit_timing_text_label),
                text = currentText,
                defaultText = defaultText,
                isTextToSpeechAvailable = available,
                onTextChanged = changeText,
                onPreviewClick = onPreviewClick,
                unknownPlaceholders =
                    findUnknownLmuWindowsPitTimingReadoutPlaceholders(currentText)
                        .joinToString("、"),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
            ) {
                AssistChip(
                    onClick = {
                        changeText(
                            currentText + LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER,
                        )
                    },
                    label = { Text(stringResource(Res.string.pit_timing_laps_insert)) },
                    enabled =
                        available &&
                            currentText.length + LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER.length <=
                            READOUT_CUSTOM_TEXT_MAX_LENGTH,
                )
                Text(
                    text = stringResource(Res.string.pit_timing_laps_placeholder_hint),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        ReadoutTextField(
            label = stringResource(Res.string.pit_timing_imminent_text_label),
            text = imminentText,
            defaultText = imminentDefaultText,
            isTextToSpeechAvailable = available,
            onTextChanged = onImminentTextChanged,
            onPreviewClick = onImminentPreviewClick,
        )
    }
}

/** 通常・切迫時の文言入力と試聴を提供する。未知トークンの警告は通常文言だけに使う。 */
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
        resetContentDescription = stringResource(Res.string.pit_timing_text_reset_to_default),
        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
        onValueChangeFinished = onTextChanged,
        onPreviewClick = onPreviewClick,
        enabled = isTextToSpeechAvailable,
        selected = text.isNotBlank(),
        supportingText =
            when {
                !isTextToSpeechAvailable -> {
                    stringResource(Res.string.pit_timing_text_unavailable)
                }

                unknownPlaceholders.isNotEmpty() -> {
                    stringResource(Res.string.pit_timing_text_unknown_placeholders, unknownPlaceholders)
                }

                text.isNotBlank() -> {
                    null
                }

                else -> {
                    stringResource(Res.string.pit_timing_text_supporting)
                }
            },
        previewContentDescription = stringResource(Res.string.pit_timing_text_preview),
        selectedContentDescription = stringResource(Res.string.pit_timing_text_selected_icon),
    )
}

@Composable
internal fun PitTimingLapsHelpSheetContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.pit_timing_laps_help_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = KoDriverSpacing.large),
    )
    Spacer(modifier = Modifier.height(KoDriverSpacing.extraLarge))
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutPitTimingDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsReadoutPitTimingDetailPaneContent()
    }
}
