package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.core.model.MyBestLapVoiceType
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5MyBestLapVoiceTypeUseCase

internal class Gt7Ps5ReadoutMyBestLapDetailViewModel(
    observeMyBestLapVoiceType: ObserveGt7Ps5MyBestLapVoiceTypeUseCase,
    private val saveMyBestLapVoiceType: SaveGt7Ps5MyBestLapVoiceTypeUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<Gt7Ps5ReadoutMyBestLapDetailUiState> =
        observeMyBestLapVoiceType()
            .map { Gt7Ps5ReadoutMyBestLapDetailUiState(voiceType = it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                Gt7Ps5ReadoutMyBestLapDetailUiState(),
            )

    fun onVoiceTypeChanged(type: MyBestLapVoiceType) {
        viewModelScope.launch {
            saveMyBestLapVoiceType(type)
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
