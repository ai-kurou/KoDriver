package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.Res
import kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_description
import kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_reset_to_default
import kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_slider_label
import kodriver.feature.gt7ps5readout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_subtitle
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val MINIMUM_REMAINING_FUEL_LAPS = 1f
private const val MAXIMUM_REMAINING_FUEL_LAPS = 5f
private const val DEFAULT_REMAINING_FUEL_LAPS = 3f

@Composable
fun Gt7Ps5ReadoutRemainingFuelLapsDetailPane(
    modifier: Modifier = Modifier,
) {
    var remainingFuelLaps by remember { mutableFloatStateOf(DEFAULT_REMAINING_FUEL_LAPS) }
    Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
        remainingFuelLaps = remainingFuelLaps,
        onRemainingFuelLapsChanged = { remainingFuelLaps = it },
        onResetRemainingFuelLaps = { remainingFuelLaps = DEFAULT_REMAINING_FUEL_LAPS },
        modifier = modifier,
    )
}

@Composable
internal fun Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
    remainingFuelLaps: Float,
    onRemainingFuelLapsChanged: (Float) -> Unit,
    onResetRemainingFuelLaps: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sliderLabel = stringResource(Res.string.remaining_fuel_laps_slider_label)
    val resetToDefaultLabel = stringResource(Res.string.remaining_fuel_laps_reset_to_default)

    Column(modifier = modifier.fillMaxSize()) {
        DetailPaneDescription(text = stringResource(Res.string.remaining_fuel_laps_description))
        DetailPaneSubtitle(text = stringResource(Res.string.remaining_fuel_laps_subtitle))
        ThresholdSlider(
            value = remainingFuelLaps,
            valueRange = MINIMUM_REMAINING_FUEL_LAPS..MAXIMUM_REMAINING_FUEL_LAPS,
            labelFormatter = { sliderLabel.format(it.roundToInt()) },
            onValueChangeFinished = onRemainingFuelLapsChanged,
            steps = 3,
            defaultValue = DEFAULT_REMAINING_FUEL_LAPS,
            onResetToDefault = onResetRemainingFuelLaps,
            resetContentDescription = resetToDefaultLabel,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Gt7Ps5ReadoutRemainingFuelLapsDetailPanePreview() {
    Gt7Ps5ReadoutRemainingFuelLapsDetailPane()
}
