package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.HeatLevel
import kurou.kodriver.core.designsystem.DebugStateHeatTile as CommonDebugStateHeatTile

@Composable
internal fun DebugStateHeatTile(
    text: String,
    celsius: Double?,
    modifier: Modifier = Modifier,
    heatLevel: HeatLevel? = celsius?.let { tyreTemperatureHeatLevel(it) },
) {
    CommonDebugStateHeatTile(text = text, heatLevel = heatLevel, modifier = modifier)
}
