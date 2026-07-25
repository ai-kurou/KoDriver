package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.Res
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_description
import kurou.kodriver.core.designsystem.DetailPaneDescription
import org.jetbrains.compose.resources.stringResource

@Composable
fun LmuWindowsReadoutPitTimingDetailPane(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.pit_timing_description),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutPitTimingDetailPanePreview() {
    LmuWindowsReadoutPitTimingDetailPane()
}
