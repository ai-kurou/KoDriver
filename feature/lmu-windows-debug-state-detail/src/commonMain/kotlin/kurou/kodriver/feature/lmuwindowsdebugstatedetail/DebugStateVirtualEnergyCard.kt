package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_virtual_energy_per_lap_ratio
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_virtual_energy_remaining_laps
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_virtual_energy_remaining_percent
import org.jetbrains.compose.resources.stringResource
import kotlin.math.round

@Composable
internal fun VirtualEnergyContent(
    virtualEnergy: LmuWindowsVirtualEnergyData?,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
) {
    val remainingPercent = calculateLmuVirtualEnergyRemainingPercent(virtualEnergy)
    if (remainingPercent == null) {
        DebugStateUnavailableContent()
        return
    }
    val consumption = calculateLmuVirtualEnergyConsumption(virtualEnergy, lmuWindowsTelemetry)
    Column {
        Text(
            text =
                stringResource(
                    Res.string.debug_state_virtual_energy_remaining_percent,
                    formatOneDecimal(remainingPercent),
                ),
            style = koDriverNumericTextStyle(MaterialTheme.typography.headlineMedium),
        )
        if (consumption != null) {
            Text(
                text =
                    stringResource(
                        Res.string.debug_state_virtual_energy_per_lap_ratio,
                        formatOneDecimal(consumption.consumptionPerLap),
                    ),
                style = koDriverNumericTextStyle(),
            )
            Text(
                text =
                    stringResource(
                        Res.string.debug_state_virtual_energy_remaining_laps,
                        formatOneDecimal(consumption.preciseRemainingLaps),
                    ),
                style = koDriverNumericTextStyle(),
            )
        }
    }
}

internal fun formatOneDecimal(value: Double): String {
    val rounded = round(value * 10) / 10
    return rounded.toString()
}
