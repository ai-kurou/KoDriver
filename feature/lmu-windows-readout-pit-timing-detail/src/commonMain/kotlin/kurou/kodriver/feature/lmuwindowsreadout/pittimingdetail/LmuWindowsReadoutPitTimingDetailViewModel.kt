package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.PitTimingSource
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase

private const val PIT_TIMING_PREVIEW_LAPS = 5

internal data class PitTimingUseCases(
    val observeVirtualEnergyLaps: ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase,
    val observeTyreWearLaps: ObserveLmuWindowsPitTimingTyreWearLapsUseCase,
    val observeEnabledStates: ObserveLmuWindowsPitTimingEnabledStatesUseCase,
    val saveVirtualEnergyLaps: SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase,
    val saveTyreWearLaps: SaveLmuWindowsPitTimingTyreWearLapsUseCase,
    val saveEnabledState: SaveLmuWindowsPitTimingEnabledStateUseCase,
)

internal class LmuWindowsReadoutPitTimingDetailViewModel(
    private val pitTimingUseCases: PitTimingUseCases,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutPitTimingDetailUiState> =
        combine(
            pitTimingUseCases.observeVirtualEnergyLaps(),
            pitTimingUseCases.observeTyreWearLaps(),
            pitTimingUseCases.observeEnabledStates(),
        ) { virtualEnergyLaps, tyreWearLaps, enabledStates ->
            LmuWindowsReadoutPitTimingDetailUiState(
                virtualEnergyEnabled = enabledStates.getValue(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy),
                virtualEnergyLaps = virtualEnergyLaps,
                tyreWearEnabled = enabledStates.getValue(ReadoutItemKey.LmuWindows.PitTiming.TyreWear),
                tyreWearLaps = tyreWearLaps,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutPitTimingDetailUiState(),
        )

    fun onVirtualEnergyEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            pitTimingUseCases.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy, enabled)
        }
    }

    fun onVirtualEnergyLapsChanged(laps: Int) {
        viewModelScope.launch {
            pitTimingUseCases.saveVirtualEnergyLaps(laps)
        }
    }

    fun onTyreWearEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            pitTimingUseCases.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.TyreWear, enabled)
        }
    }

    fun onTyreWearLapsChanged(laps: Int) {
        viewModelScope.launch {
            pitTimingUseCases.saveTyreWearLaps(laps)
        }
    }

    fun onPreviewClicked() {
        playSpeechEvent(SpeechEvent.PitTimingWarning(PIT_TIMING_PREVIEW_LAPS, source = PitTimingSource.TyreWear))
        playSpeechEvent(SpeechEvent.PitTimingWarning(0, source = PitTimingSource.TyreWear), queue = true)
    }
}
