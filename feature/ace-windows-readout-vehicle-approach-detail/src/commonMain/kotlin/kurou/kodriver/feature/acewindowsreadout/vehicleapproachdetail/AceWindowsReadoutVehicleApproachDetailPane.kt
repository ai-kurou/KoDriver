package kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_description
import org.jetbrains.compose.resources.stringResource

/**
 * AceWindowsReadoutVehicleApproachDetail の画面を表示する Composable。
 */
@Composable
fun AceWindowsReadoutVehicleApproachDetailPane(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.vehicle_approach_description),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AceWindowsReadoutVehicleApproachDetailPanePreview() {
    AceWindowsReadoutVehicleApproachDetailPane()
}
