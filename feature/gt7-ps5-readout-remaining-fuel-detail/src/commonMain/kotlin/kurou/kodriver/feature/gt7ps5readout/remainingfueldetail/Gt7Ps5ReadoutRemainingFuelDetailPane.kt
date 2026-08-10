package kurou.kodriver.feature.gt7ps5readout.remainingfueldetail

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
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.Res
import kurou.kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_description
import kurou.kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_preview_label
import kurou.kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_threshold_description
import kurou.kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_threshold_label
import kurou.kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_threshold_reset
import kurou.kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_threshold_subtitle
import kurou.kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val THRESHOLD_MIN = 5f
private const val THRESHOLD_MAX = 90f

/**
 * Gt7Ps5ReadoutRemainingFuelDetail の画面を表示する Composable。
 */
@Composable
fun Gt7Ps5ReadoutRemainingFuelDetailPane(modifier: Modifier = Modifier) {
    val viewModel: Gt7Ps5ReadoutRemainingFuelDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Gt7Ps5ReadoutRemainingFuelDetailPaneContent(
        uiState = uiState,
        onThresholdChanged = viewModel::onThresholdChanged,
        onThresholdReset = viewModel::onThresholdReset,
        onPreviewClicked = viewModel::onPreviewClicked,
        modifier = modifier,
    )
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
        modifier =
            modifier
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
                        defaultValue = GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT.toFloat(),
                        onResetToDefault = onThresholdReset,
                        resetContentDescription = stringResource(Res.string.remaining_fuel_threshold_reset),
                    )
                }
            },
        )
    }
}

internal data class Gt7Ps5ReadoutRemainingFuelDetailUiState(
    val thresholdPercentage: Int = GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
)

@Preview(showBackground = true)
@Composable
private fun Gt7Ps5ReadoutRemainingFuelDetailPanePreview() {
    Gt7Ps5ReadoutRemainingFuelDetailPaneContent()
}
