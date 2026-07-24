package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
    Spacer(modifier = modifier.fillMaxSize())
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutRemainingVirtualEnergyDetailPanePreview() {
    LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent()
}
