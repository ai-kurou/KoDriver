package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsRemainingFuelLapsData
import kotlin.math.round

@Composable
internal fun FuelConsumptionContent(
    aceWindowsFuel: AceWindowsFuelData?,
    aceWindowsRemainingFuelLaps: AceWindowsRemainingFuelLapsData?,
) {
    AceWindowsFuelContent(aceWindowsFuel, aceWindowsRemainingFuelLaps)
}

internal fun formatOneDecimal(value: Double): String {
    val rounded = round(value * 10) / 10
    return rounded.toString()
}
