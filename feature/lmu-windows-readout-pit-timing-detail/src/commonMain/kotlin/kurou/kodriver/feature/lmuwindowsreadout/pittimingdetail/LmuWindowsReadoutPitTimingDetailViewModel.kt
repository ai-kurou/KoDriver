package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase

private const val PIT_TIMING_PREVIEW_LAPS = 5

internal class LmuWindowsReadoutPitTimingDetailViewModel(
    observeLmuWindowsPitTimingVirtualEnergyLaps: ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase,
    observeLmuWindowsPitTimingTyreWearLaps: ObserveLmuWindowsPitTimingTyreWearLapsUseCase,
    private val saveLmuWindowsPitTimingVirtualEnergyLaps: SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase,
    private val saveLmuWindowsPitTimingTyreWearLaps: SaveLmuWindowsPitTimingTyreWearLapsUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutPitTimingDetailUiState> =
        combine(
        observeLmuWindowsPitTimingVirtualEnergyLaps(),
        observeLmuWindowsPitTimingTyreWearLaps(),
    ) { virtualEnergyLaps, tyreWearLaps ->
        LmuWindowsReadoutPitTimingDetailUiState(
            virtualEnergyLaps = virtualEnergyLaps,
            tyreWearLaps = tyreWearLaps,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        LmuWindowsReadoutPitTimingDetailUiState(),
    )

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

    fun onPreviewClicked() {
        playSpeechEvent(SpeechEvent.PitTimingWarning(PIT_TIMING_PREVIEW_LAPS))
        playSpeechEvent(SpeechEvent.PitTimingWarning(0), queue = true)
    }
}
