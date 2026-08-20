package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.Res
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_description
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_label
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_reset
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_subtitle
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_warning_card_title
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_warning_preview_chip
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
        onPreviewClicked = viewModel::onPreviewClicked,
        modifier = modifier,
    )
}

@Composable
internal fun Gt7Ps5ReadoutTyreTemperatureDetailPaneContent(
    uiState: Gt7Ps5ReadoutTyreTemperatureDetailUiState = Gt7Ps5ReadoutTyreTemperatureDetailUiState(),
    onOverheatWarningEnabledChanged: (Boolean) -> Unit = {},
    onHighThresholdChanged: (Int) -> Unit = {},
    onHighThresholdReset: () -> Unit = {},
    onPreviewClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val labelTemplate = stringResource(Res.string.tyre_temperature_high_threshold_label)
    val overheatWarningPreviewChipLabel = stringResource(Res.string.tyre_temperature_overheat_warning_preview_chip)
    val highThresholdMin = GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN
    val highThresholdMax = GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX

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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DetailPaneCardChips(
                        chipLabels = listOf(overheatWarningPreviewChipLabel),
                        selectedChipLabels = setOf(overheatWarningPreviewChipLabel),
                        chipEnabled = uiState.overheatWarningEnabled,
                        onChipClick = { onPreviewClicked() },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
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

@Preview(showBackground = true)
@Composable
private fun Gt7Ps5ReadoutTyreTemperatureDetailPanePreview() {
    Gt7Ps5ReadoutTyreTemperatureDetailPaneContent()
}
