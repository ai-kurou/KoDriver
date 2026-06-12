package kurou.kodriver.feature.readout.vehicledamage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kodriver.feature.readout.vehicledamage.generated.resources.Res
import kodriver.feature.readout.vehicledamage.generated.resources.vehicle_damage_description
import kodriver.feature.readout.vehicledamage.generated.resources.vehicle_damage_title
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneTitle
import org.jetbrains.compose.resources.stringResource

@Composable
fun VehicleDamageDetailPane(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        DetailPaneTitle(title = stringResource(Res.string.vehicle_damage_title))
        DetailPaneDescription(text = stringResource(Res.string.vehicle_damage_description))
    }
}

@Preview(showBackground = true)
@Composable
private fun VehicleDamageDetailPanePreview() {
    VehicleDamageDetailPane()
}
