package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import kodriver.feature.lmu_windows_readout_remaining_virtual_energy_detail.generated.resources.Res
import kodriver.feature.lmu_windows_readout_remaining_virtual_energy_detail.generated.resources.remaining_virtual_energy_description
import kodriver.feature.lmu_windows_readout_remaining_virtual_energy_detail.generated.resources.remaining_virtual_energy_threshold_label
import kodriver.feature.lmu_windows_readout_remaining_virtual_energy_detail.generated.resources.remaining_virtual_energy_threshold_reset
import kodriver.feature.lmu_windows_readout_remaining_virtual_energy_detail.generated.resources.remaining_virtual_energy_warning_title
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.ThresholdSlider
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val THRESHOLD_MIN = 10f
private const val THRESHOLD_MAX = 90f
private const val THRESHOLD_DEFAULT = 50f

@Composable
fun LmuWindowsReadoutRemainingVirtualEnergyDetailPane(
    modifier: Modifier = Modifier,
) {
    // 現時点では設定項目を持たない空の detailPane。将来の設定項目追加に備えて ViewModel を配線しておく。
    koinViewModel<LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel>()
    LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(modifier = modifier)
}

@Composable
internal fun LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(
    modifier: Modifier = Modifier,
) {
    // TODO: 永続化は未実装。設定保存が必要になった時点で ViewModel/UiState/PreferencesRepository へ移行する。
    var thresholdPercentage by remember { mutableFloatStateOf(THRESHOLD_DEFAULT) }
    val thresholdLabelTemplate = stringResource(Res.string.remaining_virtual_energy_threshold_label)
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.remaining_virtual_energy_description),
        )
        DetailPaneCard(
            title = stringResource(Res.string.remaining_virtual_energy_warning_title),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                ThresholdSlider(
                    value = thresholdPercentage,
                    valueRange = THRESHOLD_MIN..THRESHOLD_MAX,
                    steps = (THRESHOLD_MAX - THRESHOLD_MIN).toInt() - 1,
                    labelFormatter = { thresholdLabelTemplate.format(it.roundToInt()) },
                    onValueChangeFinished = { thresholdPercentage = it },
                    defaultValue = THRESHOLD_DEFAULT,
                    onResetToDefault = { thresholdPercentage = THRESHOLD_DEFAULT },
                    resetContentDescription = stringResource(Res.string.remaining_virtual_energy_threshold_reset),
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutRemainingVirtualEnergyDetailPanePreview() {
    LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent()
}
