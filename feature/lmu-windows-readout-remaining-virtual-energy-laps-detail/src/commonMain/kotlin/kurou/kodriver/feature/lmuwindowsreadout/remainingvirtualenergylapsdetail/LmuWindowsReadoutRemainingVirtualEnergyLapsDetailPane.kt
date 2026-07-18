package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergylapsdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.lmuwindowsreadout.remainingvirtualenergylapsdetail.generated.resources.Res
import kodriver.feature.lmuwindowsreadout.remainingvirtualenergylapsdetail.generated.resources.remaining_virtual_energy_laps_description
import kodriver.feature.lmuwindowsreadout.remainingvirtualenergylapsdetail.generated.resources.remaining_virtual_energy_laps_enabled
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneCard
import org.jetbrains.compose.resources.stringResource

@Composable
fun LmuWindowsReadoutRemainingVirtualEnergyLapsDetailPane(modifier: Modifier = Modifier) {
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
            bottomContent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutRemainingVirtualEnergyLapsDetailPanePreview() {
    LmuWindowsReadoutRemainingVirtualEnergyLapsDetailPane()
}
