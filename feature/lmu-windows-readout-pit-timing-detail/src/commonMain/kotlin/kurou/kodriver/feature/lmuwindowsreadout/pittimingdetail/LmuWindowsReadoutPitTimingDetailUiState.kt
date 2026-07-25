package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

internal data class LmuWindowsReadoutPitTimingDetailUiState(
    val virtualEnergyEnabled: Boolean = true,
    val virtualEnergyLaps: Int = LmuWindowsReadoutPitTimingDetailViewModel.DEFAULT_LAPS,
    val tyreWearLaps: Int = LmuWindowsReadoutPitTimingDetailViewModel.DEFAULT_LAPS,
)
