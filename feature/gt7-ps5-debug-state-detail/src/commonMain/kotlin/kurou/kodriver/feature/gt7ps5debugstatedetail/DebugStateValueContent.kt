package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle

@Composable
internal fun DebugStateHeatTile(
    text: String,
    celsius: Double?,
    modifier: Modifier = Modifier,
    heatLevel: HeatLevel? = celsius?.let { tyreTemperatureHeatLevel(it) },
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
