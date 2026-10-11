package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_per_lap_liters
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_remaining_laps
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_remaining_percent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun Gt7Ps5FuelContent(gt7Ps5Telemetry: Gt7Ps5TelemetryData?) {
    val remainingPercent = calculateGt7FuelRemainingPercent(gt7Ps5Telemetry)
    if (remainingPercent == null) {
        DebugStateUnavailableContent()
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
            style = koDriverNumericTextStyle(MaterialTheme.typography.headlineMedium),
        )
        if (fuelConsumption != null) {
            Text(
                text =
                    stringResource(
                        Res.string.debug_state_fuel_consumption_per_lap_liters,
                        formatOneDecimal(fuelConsumption.consumptionPerLap),
                    ),
                style = koDriverNumericTextStyle(),
            )
            Text(
                text =
                    stringResource(
                        Res.string.debug_state_fuel_consumption_remaining_laps,
                        formatOneDecimal(fuelConsumption.preciseRemainingLaps),
                    ),
                style = koDriverNumericTextStyle(),
            )
        }
    }
}
