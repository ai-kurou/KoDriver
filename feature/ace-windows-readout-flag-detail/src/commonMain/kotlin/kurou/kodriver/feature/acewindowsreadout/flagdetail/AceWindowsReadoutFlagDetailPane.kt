package kurou.kodriver.feature.acewindowsreadout.flagdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.Res
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_description
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_title
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import org.jetbrains.compose.resources.stringResource

@Composable
fun AceWindowsReadoutFlagDetailPane(
    modifier: Modifier = Modifier,
) {
    AceWindowsReadoutFlagDetailPaneContent(modifier = modifier)
}

@Composable
internal fun AceWindowsReadoutFlagDetailPaneContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneSubtitle(
            text = stringResource(Res.string.flag_title),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        DetailPaneDescription(
            text = stringResource(Res.string.flag_description),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AceWindowsReadoutFlagDetailPanePreview() {
    AceWindowsReadoutFlagDetailPaneContent()
}
