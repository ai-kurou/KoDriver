package kurou.kodriver.feature.acewindowsreadout.mybestlapdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.formatAceWindowsMyBestLapReadoutText
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * ACE 自己ベストラップアナウンス詳細設定の ViewModel。
 *
 * 自己ベストラップ更新の有効/無効・文言はいずれも DataStore に永続化される。
 */
internal data class MyBestLapUseCases(
    val observeEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val saveEnabledState: SaveReadoutEnabledStateUseCase,
)

internal data class MyBestLapReadoutUseCases(
    val observeText: ObserveAceWindowsMyBestLapReadoutTextUseCase,
    val saveText: SaveAceWindowsMyBestLapReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class AceWindowsReadoutMyBestLapDetailViewModel(
    private val myBestLapUseCases: MyBestLapUseCases,
    private val readout: MyBestLapReadoutUseCases,
) : ViewModel() {
    private val preview =
        ReadoutTextPreviewHelper(
            viewModelScope,
            readout.checkTextToSpeechAvailable,
            readout.observeSoundVolume,
            readout.playStartSoundForKey,
            readout.speakText,
        )

    val uiState: StateFlow<AceWindowsReadoutMyBestLapDetailUiState> =
        combine(
            myBestLapUseCases.observeEnabledStates(Simulator.AceWindows.id),
            readout.observeText(),
            preview.textToSpeechAvailable,
        ) { states, text, available ->
            AceWindowsReadoutMyBestLapDetailUiState(
                enabled = states.readoutEnabled(ReadoutItemKey.AceWindows.MyBestLap.DetailEnabled),
                readoutText = text,
                isTextToSpeechAvailable = available,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AceWindowsReadoutMyBestLapDetailUiState(),
        )

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            myBestLapUseCases.saveEnabledState(
                Simulator.AceWindows.id,
                ReadoutItemKey.AceWindows.MyBestLap.DetailEnabled,
                enabled,
            )
        }
    }

    fun onReadoutTextChanged(text: String) {
        viewModelScope.launch { readout.saveText(text) }
    }

    /** サンプルタイムに置換し、空白文言・TTS利用不可・音量ゼロでは再生しない。 */
    fun onReadoutTextPreviewClicked(text: String) {
        val formattedText = formatAceWindowsMyBestLapReadoutText(text, 83_456)
        previewText(formattedText)
    }

    private fun previewText(text: String) {
        viewModelScope.launch { preview.preview(text, ReadoutItemKey.AceWindows.MyBestLap.Root) }
    }
}
