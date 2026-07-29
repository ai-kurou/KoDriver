package kurou.kodriver.feature.gt7ps5readout.remainingfueldetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.Res
import kodriver.feature.gt7ps5readout.remainingfueldetail.generated.resources.remaining_fuel_description
import kurou.kodriver.core.designsystem.DetailPaneDescription
import org.jetbrains.compose.resources.stringResource

@Composable
fun Gt7Ps5ReadoutRemainingFuelDetailPane(
    modifier: Modifier = Modifier,
) {
    Gt7Ps5ReadoutRemainingFuelDetailPaneContent(modifier = modifier)
}

@Composable
internal fun Gt7Ps5ReadoutRemainingFuelDetailPaneContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.remaining_fuel_description),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Gt7Ps5ReadoutRemainingFuelDetailPanePreview() {
    Gt7Ps5ReadoutRemainingFuelDetailPaneContent()
}
