package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_description
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_label
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_subtitle
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_warning_card_title
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_warning_chip
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_title
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val HIGH_THRESHOLD_MIN = 90f
private const val HIGH_THRESHOLD_MAX = 110f

@Composable
fun AceWindowsReadoutTyreTemperatureDetailPane(modifier: Modifier = Modifier) {
    AceWindowsReadoutTyreTemperatureDetailPaneContent(modifier = modifier)
}

@Composable
internal fun AceWindowsReadoutTyreTemperatureDetailPaneContent(modifier: Modifier = Modifier) {
    val labelTemplate = stringResource(Res.string.tyre_temperature_high_threshold_label)
    val overheatWarningChipLabel = stringResource(Res.string.tyre_temperature_overheat_warning_chip)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneSubtitle(
            text = stringResource(Res.string.tyre_temperature_title),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        DetailPaneDescription(
            text = stringResource(Res.string.tyre_temperature_description),
        )
        DetailPaneCard(
            title = stringResource(Res.string.tyre_temperature_overheat_warning_card_title),
            checked = true,
            onCheckedChange = {},
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        DetailPaneCardChips(
                            chipLabels = listOf(overheatWarningChipLabel),
                            selectedChipLabels = setOf(overheatWarningChipLabel),
                            chipEnabled = true,
                            onChipClick = {},
                        )
                    }
                    DetailPaneSubtitle(text = stringResource(Res.string.tyre_temperature_high_threshold_subtitle))
                    ThresholdSlider(
                        value = ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.toFloat(),
                        valueRange = HIGH_THRESHOLD_MIN..HIGH_THRESHOLD_MAX,
                        steps = (HIGH_THRESHOLD_MAX - HIGH_THRESHOLD_MIN).toInt() - 1,
                        labelFormatter = { labelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = {},
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AceWindowsReadoutTyreTemperatureDetailPanePreview() {
    AceWindowsReadoutTyreTemperatureDetailPaneContent()
}
