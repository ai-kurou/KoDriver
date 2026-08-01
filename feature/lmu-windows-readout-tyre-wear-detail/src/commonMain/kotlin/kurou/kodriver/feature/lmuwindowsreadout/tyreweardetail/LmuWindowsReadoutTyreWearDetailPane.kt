package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

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
import kodriver.feature.lmuwindowsreadout.tyreweardetail.generated.resources.Res
import kodriver.feature.lmuwindowsreadout.tyreweardetail.generated.resources.tyre_wear_description
import kodriver.feature.lmuwindowsreadout.tyreweardetail.generated.resources.tyre_wear_threshold_description
import kodriver.feature.lmuwindowsreadout.tyreweardetail.generated.resources.tyre_wear_threshold_label
import kodriver.feature.lmuwindowsreadout.tyreweardetail.generated.resources.tyre_wear_threshold_reset
import kodriver.feature.lmuwindowsreadout.tyreweardetail.generated.resources.tyre_wear_threshold_subtitle
import kodriver.feature.lmuwindowsreadout.tyreweardetail.generated.resources.tyre_wear_warning_chip
import kodriver.feature.lmuwindowsreadout.tyreweardetail.generated.resources.tyre_wear_warning_title
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_DEFAULT_THRESHOLD_PERCENTAGE
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val THRESHOLD_MIN = 10f
private const val THRESHOLD_MAX = 90f

/**
 * LmuWindowsReadoutTyreWearDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutTyreWearDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutTyreWearDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutTyreWearDetailPaneContent(
        uiState = uiState,
        onWarningChipClicked = viewModel::onWarningChipClicked,
        onThresholdChanged = viewModel::onThresholdChanged,
        onThresholdReset = viewModel::onThresholdReset,
        modifier = modifier,
    )
}

@Composable
internal fun LmuWindowsReadoutTyreWearDetailPaneContent(
    uiState: LmuWindowsReadoutTyreWearDetailUiState = LmuWindowsReadoutTyreWearDetailUiState(),
    onWarningChipClicked: () -> Unit = {},
    onThresholdChanged: (Int) -> Unit = {},
    onThresholdReset: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.tyre_wear_description),
        )
        val warningChipLabel = stringResource(Res.string.tyre_wear_warning_chip)
        val thresholdLabelTemplate = stringResource(Res.string.tyre_wear_threshold_label)
        DetailPaneCard(
            title = stringResource(Res.string.tyre_wear_warning_title),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DetailPaneCardChips(
                        chipLabels = listOf(warningChipLabel),
                        selectedChipLabels = setOf(warningChipLabel),
                        chipEnabled = true,
                        onChipClick = { onWarningChipClicked() },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
                    DetailPaneSubtitle(text = stringResource(Res.string.tyre_wear_threshold_subtitle))
                    DetailPaneBodyText(text = stringResource(Res.string.tyre_wear_threshold_description))
                    ThresholdSlider(
                        value = uiState.thresholdPercentage.toFloat(),
                        valueRange = THRESHOLD_MIN..THRESHOLD_MAX,
                        steps = (THRESHOLD_MAX - THRESHOLD_MIN).toInt() - 1,
                        labelFormatter = { thresholdLabelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = { onThresholdChanged(it.roundToInt()) },
                        defaultValue = LMU_WINDOWS_TYRE_WEAR_DEFAULT_THRESHOLD_PERCENTAGE.toFloat(),
                        onResetToDefault = onThresholdReset,
                        resetContentDescription = stringResource(Res.string.tyre_wear_threshold_reset),
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutTyreWearDetailPanePreview() {
    LmuWindowsReadoutTyreWearDetailPaneContent()
}
