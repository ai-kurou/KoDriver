package kurou.kodriver.feature.lmuwindowsreadout.raindetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.feature.lmuwindowsreadout.raindetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.raindetail.generated.resources.rain_description
import org.jetbrains.compose.resources.stringResource

/**
 * LmuWindowsReadoutRainDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutRainDetailPane(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DetailPaneDescription(
            text = stringResource(Res.string.rain_description),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutRainDetailPanePreview() {
    LmuWindowsReadoutRainDetailPane()
}
