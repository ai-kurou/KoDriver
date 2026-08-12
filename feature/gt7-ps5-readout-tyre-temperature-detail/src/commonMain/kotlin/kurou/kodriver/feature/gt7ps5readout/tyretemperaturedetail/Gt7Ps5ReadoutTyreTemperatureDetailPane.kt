package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.Res
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_description
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.generated.resources.tyre_temperature_title
import org.jetbrains.compose.resources.stringResource

/**
 * Gt7Ps5ReadoutTyreTemperatureDetail の画面を表示する Composable。
 */
@Composable
fun Gt7Ps5ReadoutTyreTemperatureDetailPane(modifier: Modifier = Modifier) {
    Gt7Ps5ReadoutTyreTemperatureDetailPaneContent(modifier = modifier)
}

@Composable
internal fun Gt7Ps5ReadoutTyreTemperatureDetailPaneContent(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.tyre_temperature_description),
        )
        DetailPaneCard(
            title = stringResource(Res.string.tyre_temperature_title),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Gt7Ps5ReadoutTyreTemperatureDetailPanePreview() {
    Gt7Ps5ReadoutTyreTemperatureDetailPaneContent()
}
