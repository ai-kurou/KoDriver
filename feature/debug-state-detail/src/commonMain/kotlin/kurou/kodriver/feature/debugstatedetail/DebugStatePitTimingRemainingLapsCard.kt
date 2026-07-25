package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kodriver.feature.debugstatedetail.generated.resources.Res
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kodriver.feature.debugstatedetail.generated.resources.debug_state_pit_timing_tyre_wear_remaining_laps
import kodriver.feature.debugstatedetail.generated.resources.debug_state_pit_timing_virtual_energy_remaining_laps
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.Simulator
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PitTimingRemainingLapsContent(
    selectedSimulator: Simulator?,
    virtualEnergy: LmuWindowsVirtualEnergyData?,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
) {
    if (selectedSimulator !is Simulator.LmuWindows) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    val virtualEnergyRemainingLaps = calculateLmuVirtualEnergyConsumption(virtualEnergy, lmuWindowsTelemetry)
        ?.remainingLaps
    val tyreWearRemainingLaps = calculateLmuTyreWearRemainingLaps(lmuWindowsTelemetry)
    Column {
        Text(
            text = stringResource(
                Res.string.debug_state_pit_timing_virtual_energy_remaining_laps,
                virtualEnergyRemainingLaps?.toString() ?: UNKNOWN_REMAINING_LAPS_TEXT,
            ),
        )
        Text(
            text = stringResource(
                Res.string.debug_state_pit_timing_tyre_wear_remaining_laps,
                tyreWearRemainingLaps?.toString() ?: UNKNOWN_REMAINING_LAPS_TEXT,
            ),
        )
    }
}

private const val UNKNOWN_REMAINING_LAPS_TEXT = "-"
