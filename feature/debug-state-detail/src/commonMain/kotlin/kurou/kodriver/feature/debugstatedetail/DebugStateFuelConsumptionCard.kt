package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kodriver.feature.debugstatedetail.generated.resources.Res
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_per_lap_liters
import kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_per_lap_ratio
import kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_remaining_laps
import kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_remaining_percent
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.Simulator
import org.jetbrains.compose.resources.stringResource
import kotlin.math.round

@Composable
internal fun FuelConsumptionContent(
    selectedSimulator: Simulator?,
    virtualEnergy: LmuWindowsVirtualEnergyData?,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
    gt7Ps5Telemetry: Gt7Ps5TelemetryData?,
    aceWindowsFuel: AceWindowsFuelData?,
) {
    if (selectedSimulator is Simulator.AceWindows) {
        AceWindowsFuelContent(aceWindowsFuel)
        return
    }
    val (result, perLapTextRes) = when (selectedSimulator) {
        is Simulator.LmuWindows ->
            calculateLmuVirtualEnergyConsumption(virtualEnergy, lmuWindowsTelemetry) to
                Res.string.debug_state_fuel_consumption_per_lap_ratio
        is Simulator.Gt7Ps5 ->
            calculateGt7FuelConsumption(gt7Ps5Telemetry) to
                Res.string.debug_state_fuel_consumption_per_lap_liters
        is Simulator.AceWindows, null -> null to Res.string.debug_state_fuel_consumption_per_lap_liters
    }
    if (result == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Column {
        Text(text = stringResource(perLapTextRes, formatOneDecimal(result.consumptionPerLap)))
        Text(
            text = stringResource(
                Res.string.debug_state_fuel_consumption_remaining_laps,
                formatOneDecimal(result.preciseRemainingLaps),
            ),
        )
    }
}

@Composable
private fun AceWindowsFuelContent(aceWindowsFuel: AceWindowsFuelData?) {
    if (aceWindowsFuel == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Text(
        text = stringResource(
            Res.string.debug_state_fuel_consumption_remaining_percent,
            formatOneDecimal(aceWindowsFuel.remainingPercent),
        ),
    )
}

private fun formatOneDecimal(value: Double): String {
    val rounded = round(value * 10) / 10
    return rounded.toString()
}
