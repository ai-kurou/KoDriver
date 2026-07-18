package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergylapsdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.lmuwindowsreadout.remainingvirtualenergylapsdetail.generated.resources.Res
import kodriver.feature.lmuwindowsreadout.remainingvirtualenergylapsdetail.generated.resources.remaining_virtual_energy_laps_description
import kodriver.feature.lmuwindowsreadout.remainingvirtualenergylapsdetail.generated.resources.remaining_virtual_energy_laps_enabled
import kodriver.feature.lmuwindowsreadout.remainingvirtualenergylapsdetail.generated.resources.remaining_virtual_energy_laps_reset_to_default
import kodriver.feature.lmuwindowsreadout.remainingvirtualenergylapsdetail.generated.resources.remaining_virtual_energy_laps_slider_label
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.ThresholdSlider
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val MINIMUM_REMAINING_VIRTUAL_ENERGY_LAPS = 1f
private const val MAXIMUM_REMAINING_VIRTUAL_ENERGY_LAPS = 5f
internal const val DEFAULT_REMAINING_VIRTUAL_ENERGY_LAPS = 3

@Composable
fun LmuWindowsReadoutRemainingVirtualEnergyLapsDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: LmuWindowsReadoutRemainingVirtualEnergyLapsDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutRemainingVirtualEnergyLapsDetailPaneContent(
        uiState = uiState,
        onRemainingVirtualEnergyLapsChanged = viewModel::onRemainingVirtualEnergyLapsChanged,
        onResetRemainingVirtualEnergyLaps = viewModel::onResetRemainingVirtualEnergyLaps,
        modifier = modifier,
    )
}

@Composable
internal fun LmuWindowsReadoutRemainingVirtualEnergyLapsDetailPaneContent(
    uiState: LmuWindowsReadoutRemainingVirtualEnergyLapsDetailUiState =
        LmuWindowsReadoutRemainingVirtualEnergyLapsDetailUiState(),
    onRemainingVirtualEnergyLapsChanged: (Int) -> Unit = {},
    onResetRemainingVirtualEnergyLaps: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sliderLabel = stringResource(Res.string.remaining_virtual_energy_laps_slider_label)
    val resetToDefaultLabel = stringResource(Res.string.remaining_virtual_energy_laps_reset_to_default)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneBodyText(
            text = stringResource(Res.string.remaining_virtual_energy_laps_description),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        DetailPaneCard(
            title = stringResource(Res.string.remaining_virtual_energy_laps_enabled),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ThresholdSlider(
                        value = uiState.remainingVirtualEnergyLaps.toFloat(),
                        valueRange = MINIMUM_REMAINING_VIRTUAL_ENERGY_LAPS..MAXIMUM_REMAINING_VIRTUAL_ENERGY_LAPS,
                        labelFormatter = { sliderLabel.format(it.roundToInt()) },
                        onValueChangeFinished = { onRemainingVirtualEnergyLapsChanged(it.roundToInt()) },
                        steps = 3,
                        defaultValue = DEFAULT_REMAINING_VIRTUAL_ENERGY_LAPS.toFloat(),
                        onResetToDefault = onResetRemainingVirtualEnergyLaps,
                        resetContentDescription = resetToDefaultLabel,
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutRemainingVirtualEnergyLapsDetailPanePreview() {
    LmuWindowsReadoutRemainingVirtualEnergyLapsDetailPaneContent()
}
