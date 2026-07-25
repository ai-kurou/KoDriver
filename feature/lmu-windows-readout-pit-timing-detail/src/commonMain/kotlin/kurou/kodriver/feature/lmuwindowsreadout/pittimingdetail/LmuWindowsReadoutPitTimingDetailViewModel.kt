package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase

internal class LmuWindowsReadoutPitTimingDetailViewModel(
    observeLmuWindowsPitTimingVirtualEnergyLaps: ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase,
    observeLmuWindowsPitTimingTyreWearLaps: ObserveLmuWindowsPitTimingTyreWearLapsUseCase,
    private val saveLmuWindowsPitTimingVirtualEnergyLaps: SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase,
    private val saveLmuWindowsPitTimingTyreWearLaps: SaveLmuWindowsPitTimingTyreWearLapsUseCase,
) : ViewModel() {

    private val _virtualEnergyEnabled = MutableStateFlow(true)

    val uiState: StateFlow<LmuWindowsReadoutPitTimingDetailUiState> = combine(
        _virtualEnergyEnabled,
        observeLmuWindowsPitTimingVirtualEnergyLaps(),
        observeLmuWindowsPitTimingTyreWearLaps(),
    ) { virtualEnergyEnabled, virtualEnergyLaps, tyreWearLaps ->
        LmuWindowsReadoutPitTimingDetailUiState(
            virtualEnergyEnabled = virtualEnergyEnabled,
            virtualEnergyLaps = virtualEnergyLaps,
            tyreWearLaps = tyreWearLaps,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        LmuWindowsReadoutPitTimingDetailUiState(),
    )

    fun onVirtualEnergyEnabledChanged(enabled: Boolean) {
        _virtualEnergyEnabled.update { enabled }
    }

    fun onVirtualEnergyLapsChanged(laps: Int) {
        viewModelScope.launch {
            saveLmuWindowsPitTimingVirtualEnergyLaps(laps)
        }
    }

    fun onTyreWearLapsChanged(laps: Int) {
        viewModelScope.launch {
            saveLmuWindowsPitTimingTyreWearLaps(laps)
        }
    }
}
