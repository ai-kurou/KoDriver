package kurou.kodriver.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

/** 明示的な熱レベルで値を表示し、未取得時は中立色を使う。 */
@Composable
fun DebugStateHeatTile(
    text: String,
    heatLevel: HeatLevel?,
    modifier: Modifier = Modifier,
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

/** 状態をアクセント色のチップで表示する。 */
@Composable
fun DebugStateStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.tertiary,
) {
    Text(
        text = text,
        modifier =
            modifier
                .background(accent.copy(alpha = 0.16f), MaterialTheme.shapes.large)
                .padding(horizontal = KoDriverSpacing.medium, vertical = KoDriverSpacing.extraSmall),
        color = accent,
        style = MaterialTheme.typography.labelLarge,
    )
}
