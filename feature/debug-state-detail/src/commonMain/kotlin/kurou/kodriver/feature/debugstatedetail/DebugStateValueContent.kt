package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle

@Composable
internal fun DebugStateHeatTile(
    text: String,
    celsius: Double?,
    modifier: Modifier = Modifier,
    heatLevel: HeatLevel? = celsius?.let { temperatureHeatLevel(it) },
) {
    val color = heatLevel?.let { heatColor(it) } ?: MaterialTheme.colorScheme.outlineVariant
    Text(
        text = text,
        modifier =
            modifier
                .background(color.copy(alpha = 0.22f), MaterialTheme.shapes.medium)
                .padding(KoDriverSpacing.small),
        style = koDriverNumericTextStyle(MaterialTheme.typography.titleLarge),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
    )
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
    Text(
        text = text,
        modifier =
            Modifier
                .background(accent.copy(alpha = 0.16f), MaterialTheme.shapes.large)
                .padding(horizontal = KoDriverSpacing.medium, vertical = KoDriverSpacing.extraSmall),
        color = accent,
        style = MaterialTheme.typography.labelLarge,
    )
}
