package kurou.kodriver.feature.acewindowsreadout.mybestlapdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.usecase.ObserveAceWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsMyBestLapVoiceTypeUseCase

internal class AceWindowsReadoutMyBestLapDetailViewModel(
    observeMyBestLapVoiceType: ObserveAceWindowsMyBestLapVoiceTypeUseCase,
    private val saveMyBestLapVoiceType: SaveAceWindowsMyBestLapVoiceTypeUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<AceWindowsReadoutMyBestLapDetailUiState> =
        observeMyBestLapVoiceType()
            .map { AceWindowsReadoutMyBestLapDetailUiState(voiceType = it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                AceWindowsReadoutMyBestLapDetailUiState(),
            )

    fun onVoiceTypeChanged(type: MyBestLapVoiceType) {
        viewModelScope.launch {
            saveMyBestLapVoiceType(type)
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
