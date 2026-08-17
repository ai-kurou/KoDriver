package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_description
import kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail.generated.resources.tyre_temperature_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun AceWindowsReadoutTyreTemperatureDetailPane(modifier: Modifier = Modifier) {
    AceWindowsReadoutTyreTemperatureDetailPaneContent(modifier = modifier)
}

@Composable
internal fun AceWindowsReadoutTyreTemperatureDetailPaneContent(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneSubtitle(
            text = stringResource(Res.string.tyre_temperature_title),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        DetailPaneDescription(
            text = stringResource(Res.string.tyre_temperature_description),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AceWindowsReadoutTyreTemperatureDetailPanePreview() {
    AceWindowsReadoutTyreTemperatureDetailPaneContent()
}
