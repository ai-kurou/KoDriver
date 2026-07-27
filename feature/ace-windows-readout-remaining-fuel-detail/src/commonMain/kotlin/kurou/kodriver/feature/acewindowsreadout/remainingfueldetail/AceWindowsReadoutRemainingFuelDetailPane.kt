package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.acewindowsreadout.remainingfueldetail.generated.resources.Res
import kodriver.feature.acewindowsreadout.remainingfueldetail.generated.resources.remaining_fuel_description
import kodriver.feature.acewindowsreadout.remainingfueldetail.generated.resources.remaining_fuel_threshold_description
import kodriver.feature.acewindowsreadout.remainingfueldetail.generated.resources.remaining_fuel_threshold_label
import kodriver.feature.acewindowsreadout.remainingfueldetail.generated.resources.remaining_fuel_threshold_reset
import kodriver.feature.acewindowsreadout.remainingfueldetail.generated.resources.remaining_fuel_threshold_subtitle
import kodriver.feature.acewindowsreadout.remainingfueldetail.generated.resources.remaining_fuel_title
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val THRESHOLD_MIN = 5f
private const val THRESHOLD_MAX = 90f
private const val THRESHOLD_DEFAULT = 30f

@Composable
fun AceWindowsReadoutRemainingFuelDetailPane(
    modifier: Modifier = Modifier,
) {
    AceWindowsReadoutRemainingFuelDetailPaneContent(modifier = modifier)
}

@Composable
internal fun AceWindowsReadoutRemainingFuelDetailPaneContent(
    modifier: Modifier = Modifier,
) {
    var thresholdPercentage by remember { mutableFloatStateOf(THRESHOLD_DEFAULT) }
    val thresholdLabelTemplate = stringResource(Res.string.remaining_fuel_threshold_label)

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
                    DetailPaneSubtitle(text = stringResource(Res.string.remaining_fuel_threshold_subtitle))
                    DetailPaneBodyText(text = stringResource(Res.string.remaining_fuel_threshold_description))
                    ThresholdSlider(
                        value = thresholdPercentage,
                        valueRange = THRESHOLD_MIN..THRESHOLD_MAX,
                        steps = (THRESHOLD_MAX - THRESHOLD_MIN).toInt() - 1,
                        labelFormatter = { thresholdLabelTemplate.format(it.roundToInt()) },
                        onValueChangeFinished = { thresholdPercentage = it },
                        defaultValue = THRESHOLD_DEFAULT,
                        onResetToDefault = { thresholdPercentage = THRESHOLD_DEFAULT },
                        resetContentDescription = stringResource(Res.string.remaining_fuel_threshold_reset),
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AceWindowsReadoutRemainingFuelDetailPanePreview() {
    AceWindowsReadoutRemainingFuelDetailPaneContent()
}
