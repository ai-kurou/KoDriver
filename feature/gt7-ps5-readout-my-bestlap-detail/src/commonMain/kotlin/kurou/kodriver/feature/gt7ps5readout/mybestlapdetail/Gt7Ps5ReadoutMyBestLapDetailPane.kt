package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.gt7ps5readout.mybestlapdetail.generated.resources.Res
import kodriver.feature.gt7ps5readout.mybestlapdetail.generated.resources.my_best_lap_description
import kodriver.feature.gt7ps5readout.mybestlapdetail.generated.resources.my_best_lap_enabled
import kodriver.feature.gt7ps5readout.mybestlapdetail.generated.resources.my_best_lap_subtitle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import org.jetbrains.compose.resources.stringResource

@Composable
fun Gt7Ps5ReadoutMyBestLapDetailPane(
    modifier: Modifier = Modifier,
) {
    Gt7Ps5ReadoutMyBestLapDetailPaneContent(modifier = modifier)
}

@Composable
internal fun Gt7Ps5ReadoutMyBestLapDetailPaneContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(text = stringResource(Res.string.my_best_lap_description))
        DetailPaneSubtitle(text = stringResource(Res.string.my_best_lap_subtitle))
        DetailPaneCard(
            title = stringResource(Res.string.my_best_lap_enabled),
            chipLabels = emptyList(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Gt7Ps5ReadoutMyBestLapDetailPanePreview() {
    Gt7Ps5ReadoutMyBestLapDetailPaneContent()
}
