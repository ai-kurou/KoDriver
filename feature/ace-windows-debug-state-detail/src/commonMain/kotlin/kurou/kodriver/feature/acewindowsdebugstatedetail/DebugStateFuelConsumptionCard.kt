package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.domain.acewindows.model.AceWindowsFuelData
import kurou.kodriver.domain.acewindows.model.AceWindowsRemainingFuelLapsData

@Composable
internal fun FuelConsumptionContent(
    aceWindowsFuel: AceWindowsFuelData?,
    aceWindowsRemainingFuelLaps: AceWindowsRemainingFuelLapsData?,
) {
    AceWindowsFuelContent(aceWindowsFuel, aceWindowsRemainingFuelLaps)
}
