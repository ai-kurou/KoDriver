package kurou.kodriver.feature.acewindowsreadout.mybestlapdetail

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
import kurou.kodriver.domain.usecase.ObserveAceWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase

internal data class MyBestLapUseCases(
    val observeVoiceType: ObserveAceWindowsMyBestLapVoiceTypeUseCase,
    val saveVoiceType: SaveAceWindowsMyBestLapVoiceTypeUseCase,
    val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
)

internal class AceWindowsReadoutMyBestLapDetailViewModel(
    private val myBestLapUseCases: MyBestLapUseCases,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<AceWindowsReadoutMyBestLapDetailUiState> =
        combine(
            myBestLapUseCases.observeVoiceType(),
            myBestLapUseCases.observeReadoutEnabledStates(Simulator.AceWindows.id),
        ) { voiceType, enabledStates ->
            AceWindowsReadoutMyBestLapDetailUiState(
                voiceType = voiceType,
                enabled = enabledStates.readoutEnabled(ReadoutItemKey.AceWindows.MyBestLap.DetailEnabled),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AceWindowsReadoutMyBestLapDetailUiState(),
        )

    fun onVoiceTypeChanged(type: MyBestLapVoiceType) {
        viewModelScope.launch {
            myBestLapUseCases.saveVoiceType(type)
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            myBestLapUseCases.saveReadoutEnabledState(
                Simulator.AceWindows.id,
                ReadoutItemKey.AceWindows.MyBestLap.DetailEnabled,
                enabled,
            )
        }
    }

    fun onPreviewClicked(type: MyBestLapVoiceType) {
        val event =
            when (type) {
                MyBestLapVoiceType.FORMAL -> SpeechEvent.AceWindowsMyBestLapFormal
                MyBestLapVoiceType.CASUAL -> SpeechEvent.AceWindowsMyBestLapCasual
            }
        playSpeechEvent(event)
    }
}
