package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_per_lap_liters
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_per_lap_ratio
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_remaining_laps
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_remaining_percent
import org.jetbrains.compose.resources.stringResource
import kotlin.math.round

@Composable
internal fun FuelConsumptionContent(
    selectedSimulator: Simulator,
    virtualEnergy: LmuWindowsVirtualEnergyData?,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
    gt7Ps5Telemetry: Gt7Ps5TelemetryData?,
    aceWindowsFuel: AceWindowsFuelData?,
) {
    when (selectedSimulator) {
        is Simulator.AceWindows -> AceWindowsFuelContent(aceWindowsFuel)
        is Simulator.Gt7Ps5 -> Gt7Ps5FuelContent(gt7Ps5Telemetry)
        is Simulator.LmuWindows -> LmuWindowsFuelContent(virtualEnergy, lmuWindowsTelemetry)
    }
}

@Composable
private fun LmuWindowsFuelContent(
    virtualEnergy: LmuWindowsVirtualEnergyData?,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
) {
    val remainingPercent = calculateLmuVirtualEnergyRemainingPercent(virtualEnergy)
    if (remainingPercent == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    val consumption = calculateLmuVirtualEnergyConsumption(virtualEnergy, lmuWindowsTelemetry)
    Column {
        Text(
            text =
                stringResource(
                    Res.string.debug_state_fuel_consumption_remaining_percent,
                    formatOneDecimal(remainingPercent),
                ),
        )
        if (consumption != null) {
            Text(
                text =
                    stringResource(
                        Res.string.debug_state_fuel_consumption_per_lap_ratio,
                        formatOneDecimal(consumption.consumptionPerLap),
                    ),
            )
            Text(
                text =
                    stringResource(
                        Res.string.debug_state_fuel_consumption_remaining_laps,
                        formatOneDecimal(consumption.preciseRemainingLaps),
                    ),
            )
        }
    }
}

@Composable
private fun Gt7Ps5FuelContent(gt7Ps5Telemetry: Gt7Ps5TelemetryData?) {
    val remainingPercent = calculateGt7FuelRemainingPercent(gt7Ps5Telemetry)
    if (remainingPercent == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    val fuelConsumption = calculateGt7FuelConsumption(gt7Ps5Telemetry)
    Column {
        Text(
            text =
                stringResource(
                    Res.string.debug_state_fuel_consumption_remaining_percent,
                    formatOneDecimal(remainingPercent),
                ),
        )
        if (fuelConsumption != null) {
            Text(
                text =
                    stringResource(
                        Res.string.debug_state_fuel_consumption_per_lap_liters,
                        formatOneDecimal(fuelConsumption.consumptionPerLap),
                    ),
            )
            Text(
                text =
                    stringResource(
                        Res.string.debug_state_fuel_consumption_remaining_laps,
                        formatOneDecimal(fuelConsumption.preciseRemainingLaps),
                    ),
            )
        }
    }
}

@Composable
private fun AceWindowsFuelContent(aceWindowsFuel: AceWindowsFuelData?) {
    if (aceWindowsFuel == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Text(
        text =
            stringResource(
                Res.string.debug_state_fuel_consumption_remaining_percent,
                formatOneDecimal(aceWindowsFuel.remainingPercent.value),
            ),
    )
}

private fun formatOneDecimal(value: Double): String {
    val rounded = round(value * 10) / 10
    return rounded.toString()
}
