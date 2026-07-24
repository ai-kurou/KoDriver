package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.lmu_windows_readout_remaining_virtual_energy_detail.generated.resources.Res
import kodriver.feature.lmu_windows_readout_remaining_virtual_energy_detail.generated.resources.remaining_virtual_energy_description
import kodriver.feature.lmu_windows_readout_remaining_virtual_energy_detail.generated.resources.remaining_virtual_energy_warning_title
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
            bottomContent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutRemainingVirtualEnergyDetailPanePreview() {
    LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent()
}
