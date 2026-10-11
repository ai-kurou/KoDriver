package kurou.kodriver.feature.debugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.Simulator
import kotlin.math.round

@Composable
internal fun FuelConsumptionContent(
    selectedSimulator: Simulator,
    virtualEnergy: LmuWindowsVirtualEnergyData?,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
    gt7Ps5Telemetry: Gt7Ps5TelemetryData?,
    aceWindowsFuel: AceWindowsFuelData?,
    aceWindowsRemainingFuelLaps: AceWindowsRemainingFuelLapsData?,
) {
    when (selectedSimulator) {
        is Simulator.AceWindows -> AceWindowsFuelContent(aceWindowsFuel, aceWindowsRemainingFuelLaps)
        is Simulator.Gt7Ps5 -> Gt7Ps5FuelContent(gt7Ps5Telemetry)
        is Simulator.LmuWindows -> LmuWindowsFuelContent(virtualEnergy, lmuWindowsTelemetry)
    }
}

internal fun formatOneDecimal(value: Double): String {
    val rounded = round(value * 10) / 10
    return rounded.toString()
}
