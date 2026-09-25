package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail.generated.resources.brake_temperature_description
import org.jetbrains.compose.resources.stringResource

/**
 * LmuWindowsReadoutBrakeTemperatureDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutBrakeTemperatureDetailPane(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.brake_temperature_description),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutBrakeTemperatureDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsReadoutBrakeTemperatureDetailPane()
    }
}
