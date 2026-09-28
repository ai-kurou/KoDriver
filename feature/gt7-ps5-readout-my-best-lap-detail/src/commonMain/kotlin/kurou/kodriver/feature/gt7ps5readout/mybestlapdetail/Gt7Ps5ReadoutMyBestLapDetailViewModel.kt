package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase

internal class Gt7Ps5ReadoutMyBestLapDetailViewModel(
    observeMyBestLapVoiceType: ObserveGt7Ps5MyBestLapVoiceTypeUseCase,
    private val saveMyBestLapVoiceType: SaveGt7Ps5MyBestLapVoiceTypeUseCase,
    observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    private val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<Gt7Ps5ReadoutMyBestLapDetailUiState> =
        combine(
            observeMyBestLapVoiceType(),
            observeReadoutEnabledStates(Simulator.Gt7Ps5.id),
        ) { voiceType, enabledStates ->
            Gt7Ps5ReadoutMyBestLapDetailUiState(
                voiceType = voiceType,
                enabled = enabledStates.readoutEnabled(ReadoutItemKey.Gt7Ps5.MyBestLap.DetailEnabled),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Gt7Ps5ReadoutMyBestLapDetailUiState(),
        )

    fun onVoiceTypeChanged(type: MyBestLapVoiceType) {
        viewModelScope.launch {
            saveMyBestLapVoiceType(type)
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveReadoutEnabledState(Simulator.Gt7Ps5.id, ReadoutItemKey.Gt7Ps5.MyBestLap.DetailEnabled, enabled)
        }
    }

    fun onPreviewClicked(type: MyBestLapVoiceType) {
        val event =
            when (type) {
                MyBestLapVoiceType.FORMAL -> SpeechEvent.Gt7Ps5MyBestLapFormal
                MyBestLapVoiceType.CASUAL -> SpeechEvent.Gt7Ps5MyBestLapCasual
            }
        playSpeechEvent(event)
    }
}
