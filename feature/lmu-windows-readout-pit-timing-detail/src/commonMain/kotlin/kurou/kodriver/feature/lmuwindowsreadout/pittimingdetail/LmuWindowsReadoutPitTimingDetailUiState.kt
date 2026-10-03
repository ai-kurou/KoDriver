package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT

internal data class LmuWindowsReadoutPitTimingDetailUiState(
    val virtualEnergyEnabled: Boolean = true,
    val virtualEnergyLaps: Int = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT,
    val virtualEnergyText: String = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT,
    val virtualEnergyImminentText: String = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT,
    val isTextToSpeechAvailable: Boolean = false,
    val tyreWearEnabled: Boolean = true,
    val tyreWearLaps: Int = LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT,
    val tyreWearText: String = LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT,
    val tyreWearImminentText: String = LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT,
)
