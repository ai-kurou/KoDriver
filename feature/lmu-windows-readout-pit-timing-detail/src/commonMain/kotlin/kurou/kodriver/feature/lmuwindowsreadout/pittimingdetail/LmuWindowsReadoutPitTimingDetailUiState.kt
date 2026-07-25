package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT

internal data class LmuWindowsReadoutPitTimingDetailUiState(
    val virtualEnergyLaps: Int = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT,
    val tyreWearLaps: Int = LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT,
)
