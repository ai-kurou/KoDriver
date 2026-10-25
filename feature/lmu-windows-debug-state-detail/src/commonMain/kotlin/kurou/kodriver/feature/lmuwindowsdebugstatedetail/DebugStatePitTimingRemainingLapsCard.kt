package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_pit_timing_fuel_remaining_laps
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_pit_timing_tyre_wear_remaining_laps
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_pit_timing_virtual_energy_remaining_laps
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PitTimingRemainingLapsContent(
    virtualEnergy: LmuWindowsVirtualEnergyData?,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
    fuelBaseline: LmuWindowsFuelBaseline?,
) {
    val virtualEnergyRemainingLaps =
        calculateLmuVirtualEnergyConsumption(virtualEnergy, lmuWindowsTelemetry)
            ?.preciseRemainingLaps
    val tyreWearRemainingLaps = calculateLmuTyreWearRemainingLaps(lmuWindowsTelemetry)
    val fuelRemainingLaps = calculateLmuFuelRemainingLaps(lmuWindowsTelemetry, fuelBaseline)
    Column {
        Text(
            text =
                stringResource(
                    Res.string.debug_state_pit_timing_virtual_energy_remaining_laps,
                    virtualEnergyRemainingLaps?.let { formatOneDecimal(it) } ?: UNKNOWN_REMAINING_LAPS_TEXT,
                ),
            style = koDriverNumericTextStyle(),
        )
        Text(
            text =
                stringResource(
                    Res.string.debug_state_pit_timing_tyre_wear_remaining_laps,
                    tyreWearRemainingLaps?.let { formatOneDecimal(it) } ?: UNKNOWN_REMAINING_LAPS_TEXT,
                ),
            style = koDriverNumericTextStyle(),
        )
        Text(
            text =
                stringResource(
                    Res.string.debug_state_pit_timing_fuel_remaining_laps,
                    fuelRemainingLaps?.let { formatOneDecimal(it) } ?: UNKNOWN_REMAINING_LAPS_TEXT,
                ),
            style = koDriverNumericTextStyle(),
        )
    }
}

private const val UNKNOWN_REMAINING_LAPS_TEXT = "-"
