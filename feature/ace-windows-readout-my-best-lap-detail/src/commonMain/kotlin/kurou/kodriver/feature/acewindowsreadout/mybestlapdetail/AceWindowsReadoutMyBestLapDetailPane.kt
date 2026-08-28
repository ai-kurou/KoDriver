package kurou.kodriver.feature.acewindowsreadout.mybestlapdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.feature.acewindowsreadout.mybestlapdetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsreadout.mybestlapdetail.generated.resources.my_best_lap_description
import kurou.kodriver.feature.acewindowsreadout.mybestlapdetail.generated.resources.my_best_lap_title
import org.jetbrains.compose.resources.stringResource

/**
 * AceWindowsReadoutMyBestLapDetail の画面を表示する Composable。
 * 対応する SpeechEvent・WAV が未整備のため、現時点ではタイトルと説明のみを表示する。
 */
@Composable
fun AceWindowsReadoutMyBestLapDetailPane(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(Res.string.my_best_lap_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        DetailPaneDescription(
            text = stringResource(Res.string.my_best_lap_description),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AceWindowsReadoutMyBestLapDetailPanePreview() {
    AceWindowsReadoutMyBestLapDetailPane()
}
