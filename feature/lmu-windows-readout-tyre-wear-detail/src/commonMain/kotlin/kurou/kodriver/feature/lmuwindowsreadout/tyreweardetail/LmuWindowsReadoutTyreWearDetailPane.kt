package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.lmuwindowsreadout.tyreweardetail.generated.resources.Res
import kodriver.feature.lmuwindowsreadout.tyreweardetail.generated.resources.tyre_wear_description
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import org.jetbrains.compose.resources.stringResource

@Composable
fun LmuWindowsReadoutTyreWearDetailPane(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneBodyText(
            text = stringResource(Res.string.tyre_wear_description),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutTyreWearDetailPanePreview() {
    LmuWindowsReadoutTyreWearDetailPane()
}
