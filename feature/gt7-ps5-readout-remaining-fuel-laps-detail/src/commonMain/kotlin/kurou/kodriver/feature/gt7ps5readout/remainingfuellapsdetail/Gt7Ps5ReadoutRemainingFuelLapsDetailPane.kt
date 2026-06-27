package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.Res
import kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_description
import kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_enabled
import kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_readout_subtitle
import kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_reset_to_default
import kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_slider_label
import kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_subtitle
import kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_voice_type
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val MINIMUM_REMAINING_FUEL_LAPS = 0f
private const val MAXIMUM_REMAINING_FUEL_LAPS = 5f
internal const val DEFAULT_REMAINING_FUEL_LAPS = 3

@Composable
fun Gt7Ps5ReadoutRemainingFuelLapsDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
        uiState = uiState,
        onRemainingFuelLapsChanged = viewModel::onRemainingFuelLapsChanged,
        onResetRemainingFuelLaps = viewModel::onResetRemainingFuelLaps,
        onPreviewClicked = viewModel::onPreviewClicked,
        modifier = modifier,
    )
}

@Composable
internal fun Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
    uiState: Gt7Ps5ReadoutRemainingFuelLapsDetailUiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(),
    onRemainingFuelLapsChanged: (Int) -> Unit = {},
    onResetRemainingFuelLaps: () -> Unit = {},
    onPreviewClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sliderLabel = stringResource(Res.string.remaining_fuel_laps_slider_label)
    val resetToDefaultLabel = stringResource(Res.string.remaining_fuel_laps_reset_to_default)
    val voiceTypeLabel = stringResource(
        Res.string.remaining_fuel_laps_voice_type,
        uiState.remainingFuelLaps,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(text = stringResource(Res.string.remaining_fuel_laps_description))
        DetailPaneSubtitle(text = stringResource(Res.string.remaining_fuel_laps_subtitle))
        ThresholdSlider(
            value = uiState.remainingFuelLaps.toFloat(),
            valueRange = MINIMUM_REMAINING_FUEL_LAPS..MAXIMUM_REMAINING_FUEL_LAPS,
            labelFormatter = { sliderLabel.format(it.roundToInt()) },
            onValueChangeFinished = { onRemainingFuelLapsChanged(it.roundToInt()) },
            steps = 4,
            defaultValue = DEFAULT_REMAINING_FUEL_LAPS.toFloat(),
            onResetToDefault = onResetRemainingFuelLaps,
            resetContentDescription = resetToDefaultLabel,
        )
        DetailPaneSubtitle(text = stringResource(Res.string.remaining_fuel_laps_readout_subtitle))
        DetailPaneCard(
            title = stringResource(Res.string.remaining_fuel_laps_enabled),
            chipLabels = listOf(voiceTypeLabel),
            selectedChipLabels = setOf(voiceTypeLabel),
            onChipClick = { onPreviewClicked() },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Gt7Ps5ReadoutRemainingFuelLapsDetailPanePreview() {
    Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent()
}
