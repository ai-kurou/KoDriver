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
import kotlin.math.round

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
    val virtualEnergyRemainingLaps =
        calculateLmuVirtualEnergyConsumption(virtualEnergy, lmuWindowsTelemetry)
            ?.preciseRemainingLaps
    val tyreWearRemainingLaps = calculateLmuTyreWearRemainingLaps(lmuWindowsTelemetry)
    Column {
        Text(
            text =
                stringResource(
                    Res.string.debug_state_pit_timing_virtual_energy_remaining_laps,
                    virtualEnergyRemainingLaps?.let { formatOneDecimal(it) } ?: UNKNOWN_REMAINING_LAPS_TEXT,
                ),
        )
        Text(
            text =
                stringResource(
                    Res.string.debug_state_pit_timing_tyre_wear_remaining_laps,
                    tyreWearRemainingLaps?.let { formatOneDecimal(it) } ?: UNKNOWN_REMAINING_LAPS_TEXT,
                ),
        )
    }
}

private fun formatOneDecimal(value: Double): String {
    val rounded = round(value * 10) / 10
    return rounded.toString()
}

private const val UNKNOWN_REMAINING_LAPS_TEXT = "-"
