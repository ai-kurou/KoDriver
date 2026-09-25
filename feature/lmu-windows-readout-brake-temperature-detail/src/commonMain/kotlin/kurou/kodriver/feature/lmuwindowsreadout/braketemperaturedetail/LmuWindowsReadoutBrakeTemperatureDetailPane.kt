package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

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
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_description
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_description
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_label
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_reset
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_subtitle
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_warning_chip
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_warning_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val THRESHOLD_MIN = LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN.toFloat()
private const val THRESHOLD_MAX = LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX.toFloat()

/**
 * LmuWindowsReadoutBrakeTemperatureDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutBrakeTemperatureDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutBrakeTemperatureDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutBrakeTemperatureDetailPaneContent(
        uiState = uiState,
        onWarningChipClicked = viewModel::onWarningChipClicked,
        onThresholdChanged = viewModel::onThresholdChanged,
        onThresholdReset = viewModel::onThresholdReset,
        modifier = modifier,
    )
}

@Composable
internal fun LmuWindowsReadoutBrakeTemperatureDetailPaneContent(
    uiState: LmuWindowsReadoutBrakeTemperatureDetailUiState = LmuWindowsReadoutBrakeTemperatureDetailUiState(),
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
            text = stringResource(Res.string.brake_temperature_description),
        )
        val warningChipLabel = stringResource(Res.string.brake_temperature_warning_chip)
        val thresholdLabelTemplate = stringResource(Res.string.brake_temperature_threshold_label)
        DetailPaneCard(
            title = stringResource(Res.string.brake_temperature_warning_title),
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DetailPaneCardChips(
                        chipLabels = listOf(warningChipLabel),
                        selectedChipLabels = setOf(warningChipLabel),
                        chipEnabled = true,
                        onChipClick = { onWarningChipClicked() },
                    )
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.small,
                            ),
                    )
                    DetailPaneSubtitle(text = stringResource(Res.string.brake_temperature_threshold_subtitle))
                    DetailPaneBodyText(
                        text =
                            stringResource(Res.string.brake_temperature_threshold_description)
                                .formatSliderLabel(uiState.highThresholdCelsius),
                    )
                    ThresholdSlider(
                        value = uiState.highThresholdCelsius.toFloat(),
                        valueRange = THRESHOLD_MIN..THRESHOLD_MAX,
                        steps = (THRESHOLD_MAX - THRESHOLD_MIN).toInt() - 1,
                        labelFormatter = { thresholdLabelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = { onThresholdChanged(it.roundToInt()) },
                        defaultValue = LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.toFloat(),
                        onResetToDefault = onThresholdReset,
                        resetContentDescription = stringResource(Res.string.brake_temperature_threshold_reset),
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutBrakeTemperatureDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsReadoutBrakeTemperatureDetailPaneContent()
    }
}
