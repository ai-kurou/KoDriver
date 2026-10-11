package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
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
