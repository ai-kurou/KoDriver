package kurou.kodriver.feature.gt7ps5readout.remainingfueldetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.Res
import kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_description
import kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_preview_label
import kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_threshold_description
import kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_threshold_label
import kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_threshold_reset
import kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_threshold_subtitle
import kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_title
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val THRESHOLD_MIN = 5f
private const val THRESHOLD_MAX = 90f
private const val DEFAULT_THRESHOLD_PERCENTAGE = 30

@Composable
fun Gt7Ps5ReadoutRemainingFuelDetailPane(
    modifier: Modifier = Modifier,
) {
    Gt7Ps5ReadoutRemainingFuelDetailPaneContent(modifier = modifier)
}

@Composable
internal fun Gt7Ps5ReadoutRemainingFuelDetailPaneContent(
    uiState: Gt7Ps5ReadoutRemainingFuelDetailUiState = Gt7Ps5ReadoutRemainingFuelDetailUiState(),
    onThresholdChanged: (Int) -> Unit = {},
    onThresholdReset: () -> Unit = {},
    onPreviewClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val thresholdLabelTemplate = stringResource(Res.string.remaining_fuel_threshold_label)
    val previewLabel = stringResource(Res.string.remaining_fuel_preview_label)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.remaining_fuel_description),
        )
        DetailPaneCard(
            title = stringResource(Res.string.remaining_fuel_title),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DetailPaneCardChips(
                        chipLabels = listOf(previewLabel),
                        selectedChipLabels = setOf(previewLabel),
                        chipEnabled = true,
                        onChipClick = { onPreviewClicked() },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
                    DetailPaneSubtitle(text = stringResource(Res.string.remaining_fuel_threshold_subtitle))
                    DetailPaneBodyText(text = stringResource(Res.string.remaining_fuel_threshold_description))
                    ThresholdSlider(
                        value = uiState.thresholdPercentage.toFloat(),
                        valueRange = THRESHOLD_MIN..THRESHOLD_MAX,
                        steps = (THRESHOLD_MAX - THRESHOLD_MIN).toInt() - 1,
                        labelFormatter = { thresholdLabelTemplate.format(it.roundToInt()) },
                        onValueChangeFinished = { onThresholdChanged(it.roundToInt()) },
                        defaultValue = DEFAULT_THRESHOLD_PERCENTAGE.toFloat(),
                        onResetToDefault = onThresholdReset,
                        resetContentDescription = stringResource(Res.string.remaining_fuel_threshold_reset),
                    )
                }
            },
        )
    }
}

internal data class Gt7Ps5ReadoutRemainingFuelDetailUiState(
    val thresholdPercentage: Int = DEFAULT_THRESHOLD_PERCENTAGE,
)

@Preview(showBackground = true)
@Composable
private fun Gt7Ps5ReadoutRemainingFuelDetailPanePreview() {
    Gt7Ps5ReadoutRemainingFuelDetailPaneContent()
}
