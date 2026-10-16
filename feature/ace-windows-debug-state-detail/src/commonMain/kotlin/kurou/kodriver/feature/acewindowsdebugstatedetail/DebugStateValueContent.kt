package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kurou.kodriver.core.designsystem.HeatLevel
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
internal fun DebugStateStatusChip(
    text: String,
    accent: Color = MaterialTheme.colorScheme.tertiary,
) {
    CommonDebugStateStatusChip(text = text, accent = accent)
}
