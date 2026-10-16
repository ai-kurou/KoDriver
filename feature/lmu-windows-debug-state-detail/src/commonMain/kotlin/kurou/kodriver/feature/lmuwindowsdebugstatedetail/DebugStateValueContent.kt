package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kurou.kodriver.core.designsystem.HeatLevel
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.heatColor
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.core.designsystem.DebugStateHeatTile as CommonDebugStateHeatTile
import kurou.kodriver.core.designsystem.DebugStateStatusChip as CommonDebugStateStatusChip

@Composable
internal fun DebugStateHeatTile(
    text: String,
    celsius: Double?,
    modifier: Modifier = Modifier,
    heatLevel: HeatLevel? = celsius?.let { temperatureHeatLevel(it) },
) {
    CommonDebugStateHeatTile(text = text, heatLevel = heatLevel, modifier = modifier)
}

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

@Composable
internal fun DebugStateStatusChip(
    text: String,
    accent: Color = MaterialTheme.colorScheme.tertiary,
) {
    CommonDebugStateStatusChip(text = text, accent = accent)
}
