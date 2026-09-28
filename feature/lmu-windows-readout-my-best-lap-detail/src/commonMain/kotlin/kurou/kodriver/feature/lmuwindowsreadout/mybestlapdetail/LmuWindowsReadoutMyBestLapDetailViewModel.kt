package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

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
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase

internal class LmuWindowsReadoutMyBestLapDetailViewModel(
    observeMyBestLapVoiceType: ObserveLmuWindowsMyBestLapVoiceTypeUseCase,
    private val saveMyBestLapVoiceType: SaveLmuWindowsMyBestLapVoiceTypeUseCase,
    observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    private val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutMyBestLapDetailUiState> =
        combine(
            observeMyBestLapVoiceType(),
            observeReadoutEnabledStates(Simulator.LmuWindows.id),
        ) { voiceType, enabledStates ->
            LmuWindowsReadoutMyBestLapDetailUiState(
                voiceType = voiceType,
                enabled = enabledStates.readoutEnabled(ReadoutItemKey.LmuWindows.MyBestLap.DetailEnabled),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutMyBestLapDetailUiState(),
        )

    fun onVoiceTypeChanged(type: MyBestLapVoiceType) {
        viewModelScope.launch {
            saveMyBestLapVoiceType(type)
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveReadoutEnabledState(Simulator.LmuWindows.id, ReadoutItemKey.LmuWindows.MyBestLap.DetailEnabled, enabled)
        }
    }

    fun onPreviewClicked(type: MyBestLapVoiceType) {
        val event =
            when (type) {
                MyBestLapVoiceType.FORMAL -> SpeechEvent.LmuWindowsMyBestLapFormal
                MyBestLapVoiceType.CASUAL -> SpeechEvent.LmuWindowsMyBestLapCasual
            }
        playSpeechEvent(event)
    }
}
