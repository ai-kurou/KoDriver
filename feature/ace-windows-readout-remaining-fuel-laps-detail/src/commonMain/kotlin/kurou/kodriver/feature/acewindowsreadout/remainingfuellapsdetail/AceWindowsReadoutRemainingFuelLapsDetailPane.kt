package kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_description
import kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail.generated.resources.remaining_fuel_laps_title
import org.jetbrains.compose.resources.stringResource

/**
 * AceWindowsReadoutRemainingFuelLapsDetail の画面を表示する Composable。
 * 読み上げ判定・設定項目が未整備のため、現時点ではタイトルと説明のみを表示する。
 */
@Composable
fun AceWindowsReadoutRemainingFuelLapsDetailPane(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(Res.string.remaining_fuel_laps_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.large, vertical = KoDriverSpacing.extraSmall),
        )
        DetailPaneDescription(
            text = stringResource(Res.string.remaining_fuel_laps_description),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AceWindowsReadoutRemainingFuelLapsDetailPanePreview() {
    KoDriverTheme {
        AceWindowsReadoutRemainingFuelLapsDetailPane()
    }
}
