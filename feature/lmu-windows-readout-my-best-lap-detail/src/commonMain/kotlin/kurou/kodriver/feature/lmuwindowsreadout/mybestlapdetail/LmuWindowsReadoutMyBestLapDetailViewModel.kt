package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.formatLmuWindowsMyBestLapReadoutText
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * LMU 自己ベストラップアナウンス詳細設定の ViewModel。
 *
 * 自己ベストラップ更新の有効/無効・文言はいずれも DataStore に永続化される。
 */
internal data class MyBestLapUseCases(
    val observeEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val saveEnabledState: SaveReadoutEnabledStateUseCase,
)

internal data class MyBestLapReadoutUseCases(
    val observeText: ObserveLmuWindowsMyBestLapReadoutTextUseCase,
    val saveText: SaveLmuWindowsMyBestLapReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class LmuWindowsReadoutMyBestLapDetailViewModel(
    private val myBestLapUseCases: MyBestLapUseCases,
    private val readout: MyBestLapReadoutUseCases,
) : ViewModel() {
    private val textToSpeechAvailable =
        flow { emit(readout.checkTextToSpeechAvailable()) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val uiState: StateFlow<LmuWindowsReadoutMyBestLapDetailUiState> =
        combine(
            myBestLapUseCases.observeEnabledStates(Simulator.LmuWindows.id),
            readout.observeText(),
            textToSpeechAvailable,
        ) { states, text, available ->
            LmuWindowsReadoutMyBestLapDetailUiState(
                enabled = states.readoutEnabled(ReadoutItemKey.LmuWindows.MyBestLap.DetailEnabled),
                readoutText = text,
                isTextToSpeechAvailable = available,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutMyBestLapDetailUiState(),
        )

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            myBestLapUseCases.saveEnabledState(
                Simulator.LmuWindows.id,
                ReadoutItemKey.LmuWindows.MyBestLap.DetailEnabled,
                enabled,
            )
        }
    }

    fun onReadoutTextChanged(text: String) {
        viewModelScope.launch { readout.saveText(text) }
    }

    /** サンプルタイムに置換し、空白文言・TTS利用不可・音量ゼロでは再生しない。 */
    fun onReadoutTextPreviewClicked(text: String) {
        val formattedText = formatLmuWindowsMyBestLapReadoutText(text, 83_456L)
        previewText(formattedText)
    }

    private fun previewText(text: String) {
        if (text.isBlank() || !textToSpeechAvailable.value) return
        viewModelScope.launch {
            val volume = readout.observeSoundVolume().first()
            if (volume <= 0) return@launch
            readout.playStartSoundForKey(ReadoutItemKey.LmuWindows.MyBestLap.Root)
            readout.speakText(text, volume = volume)
        }
    }
}
