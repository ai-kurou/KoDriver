package kurou.kodriver.feature.readout.flag

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kodriver.feature.readout.flag.generated.resources.Res
import kodriver.feature.readout.flag.generated.resources.flag_description
import kodriver.feature.readout.flag.generated.resources.flag_switch_subtitle
import kodriver.feature.readout.flag.generated.resources.flag_title
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.DetailPaneTitle
import org.jetbrains.compose.resources.stringResource

@Composable
fun FlagDetailPane(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        DetailPaneTitle(title = stringResource(Res.string.flag_title))
        DetailPaneDescription(text = stringResource(Res.string.flag_description))
        DetailPaneSubtitle(text = stringResource(Res.string.flag_switch_subtitle))
    }
}

@Preview(showBackground = true)
@Composable
private fun FlagDetailPanePreview() {
    FlagDetailPane()
}
