package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kodriver.feature.acewindowsreadout.remainingfueldetail.generated.resources.Res
import kodriver.feature.acewindowsreadout.remainingfueldetail.generated.resources.remaining_fuel_description
import kodriver.feature.acewindowsreadout.remainingfueldetail.generated.resources.remaining_fuel_title
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import org.jetbrains.compose.resources.stringResource

@Composable
fun AceWindowsReadoutRemainingFuelDetailPane(
    modifier: Modifier = Modifier,
) {
    AceWindowsReadoutRemainingFuelDetailPaneContent(modifier = modifier)
}

@Composable
internal fun AceWindowsReadoutRemainingFuelDetailPaneContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneSubtitle(
            text = stringResource(Res.string.remaining_fuel_title),
        )
        DetailPaneDescription(
            text = stringResource(Res.string.remaining_fuel_description),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AceWindowsReadoutRemainingFuelDetailPanePreview() {
    AceWindowsReadoutRemainingFuelDetailPaneContent()
}
