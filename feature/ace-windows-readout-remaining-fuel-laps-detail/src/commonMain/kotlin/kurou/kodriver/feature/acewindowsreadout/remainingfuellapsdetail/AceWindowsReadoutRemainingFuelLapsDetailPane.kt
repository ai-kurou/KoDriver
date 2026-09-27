package kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT
import kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_description
import kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_enabled
import kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_reset_to_default
import kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_slider_label
import kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_voice_type
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val MINIMUM_REMAINING_FUEL_LAPS = 1f
private const val MAXIMUM_REMAINING_FUEL_LAPS = 5f

/**
 * AceWindowsReadoutRemainingFuelLapsDetail の画面を表示する Composable。
 */
@Composable
fun AceWindowsReadoutRemainingFuelLapsDetailPane(modifier: Modifier = Modifier) {
    val viewModel: AceWindowsReadoutRemainingFuelLapsDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AceWindowsReadoutRemainingFuelLapsDetailPaneContent(
        uiState = uiState,
        onRemainingFuelLapsChanged = viewModel::onRemainingFuelLapsChanged,
        onResetRemainingFuelLaps = viewModel::onResetRemainingFuelLaps,
        onPreviewClicked = viewModel::onPreviewClicked,
        modifier = modifier,
    )
}

@Composable
internal fun AceWindowsReadoutRemainingFuelLapsDetailPaneContent(
    uiState: AceWindowsReadoutRemainingFuelLapsDetailUiState = AceWindowsReadoutRemainingFuelLapsDetailUiState(),
    onRemainingFuelLapsChanged: (Int) -> Unit = {},
    onResetRemainingFuelLaps: () -> Unit = {},
    onPreviewClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sliderLabel = stringResource(Res.string.remaining_fuel_laps_slider_label)
    val resetToDefaultLabel = stringResource(Res.string.remaining_fuel_laps_reset_to_default)
    val voiceTypeLabel =
        stringResource(
            Res.string.remaining_fuel_laps_voice_type,
            uiState.remainingFuelLaps,
        )

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.remaining_fuel_laps_description),
        )
        DetailPaneCard(
            title = stringResource(Res.string.remaining_fuel_laps_enabled),
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DetailPaneCardChips(
                        chipLabels = listOf(voiceTypeLabel),
                        selectedChipLabels = setOf(voiceTypeLabel),
                        chipEnabled = true,
                        onChipClick = { onPreviewClicked() },
                    )
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.small,
                            ),
                    )
                    ThresholdSlider(
                        value = uiState.remainingFuelLaps.toFloat(),
                        valueRange = MINIMUM_REMAINING_FUEL_LAPS..MAXIMUM_REMAINING_FUEL_LAPS,
                        labelFormatter = { sliderLabel.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = { onRemainingFuelLapsChanged(it.roundToInt()) },
                        steps = 3,
                        defaultValue = ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT.toFloat(),
                        onResetToDefault = onResetRemainingFuelLaps,
                        resetContentDescription = resetToDefaultLabel,
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AceWindowsReadoutRemainingFuelLapsDetailPanePreview() {
    KoDriverTheme {
        AceWindowsReadoutRemainingFuelLapsDetailPaneContent()
    }
}
