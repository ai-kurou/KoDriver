package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.heatColor
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle

@Composable
internal fun DebugStateWearMeter(
    text: String,
    remainingPercent: Double?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall)) {
        Text(text = text, style = koDriverNumericTextStyle())
        // 欠損(未取得)を 0% として公開しないよう、値があるときだけメーターを出す。
        if (remainingPercent != null) {
            LinearProgressIndicator(
                progress = { (remainingPercent / 100.0).coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = heatColor(wearHeatLevel(remainingPercent)),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )
        }
    }
}
